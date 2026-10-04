#!/usr/bin/env python3
"""Build data/generated/pokedex.db from the pinned raw files (phase 1, D1-1 .. D1-23).

Inputs: data/raw/pokeapi/*.csv and data/raw/sindresorhus-pokemon/*.json, checked
against the sha256 values in data/metadata/sources.json.
Outputs (next to --out): pokedex.db, conflicts.csv, build-report.md.

The database is written to <out>.part and only moved into place when every
integrity check passes. Two builds with the same inputs and SQLite version
produce byte-identical files: rows are inserted in primary-key order and
build_date comes from sources.json, not the clock.

Run from the repository root: python3 tools/data-builder/build.py
"""

from __future__ import annotations

import argparse
import json
import os
import sqlite3
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from builder import checks  # noqa: E402
from builder.conflicts import species_name_conflicts, write_conflicts  # noqa: E402
from builder.importers import IMPORTERS  # noqa: E402
from builder.lang import lang_map  # noqa: E402
from builder.raw import ROOT, Raw, check_raw, load_sources  # noqa: E402
from builder.search import build_search_index  # noqa: E402
from builder.texts import import_flavor_texts, import_localized_names  # noqa: E402
from fetch import sha256_file  # noqa: E402

SCHEMA_VERSION = 3
SCHEMA = Path(__file__).resolve().parent / "schema.sql"
BUILD_SOURCES = ("pokeapi", "sindresorhus-pokemon")


def write_meta(conn: sqlite3.Connection, sources: dict) -> None:
    entries = sources["sources"]
    meta = {
        "schema_version": str(SCHEMA_VERSION),
        "data_version": str(sources["data_version"]),
        "build_date": max(src["retrieved"] for src in entries.values()),
        "source_versions": json.dumps({key: src["commit"] for key, src in entries.items()}, sort_keys=True, separators=(",", ":")),
    }
    conn.executemany("INSERT INTO meta (key, value) VALUES (?, ?)", sorted(meta.items()))


def populate(conn: sqlite3.Connection, raw_root: Path, sources: dict) -> list[str]:
    """Import every table; return search_index warnings."""
    pokeapi = Raw(raw_root / "pokeapi")
    langs = lang_map(pokeapi)
    for importer in IMPORTERS:
        importer(conn, pokeapi)
    import_localized_names(conn, pokeapi, langs)
    import_flavor_texts(conn, pokeapi, langs)
    warnings = build_search_index(conn)
    write_meta(conn, sources)
    return warnings


def write_report(
    path: Path,
    conn: sqlite3.Connection,
    conflicts: int,
    size: int,
    digest: str,
    errors: list[str],
    warnings: list[str],
) -> None:
    meta = dict(conn.execute("SELECT key, value FROM meta"))
    tables = [r[0] for r in conn.execute("SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name")]
    lines = [
        "# pokedex.db build report",
        "",
        f"- build_date: {meta.get('build_date', '')}",
        f"- schema_version: {meta.get('schema_version', '')}",
        f"- data_version: {meta.get('data_version', '')}",
        f"- source_versions: `{meta.get('source_versions', '')}`",
        f"- bytes: {size}",
        f"- sha256: `{digest}`",
        f"- conflicts.csv rows: {conflicts}",
        "",
        "## Rows",
        "",
        "| table | rows |",
        "|---|---|",
    ]
    for table in tables:
        (n,) = conn.execute(f'SELECT COUNT(*) FROM "{table}"').fetchone()
        lines.append(f"| `{table}` | {n} |")
    for title, items in (("Errors", errors), ("Warnings", warnings)):
        lines += ["", f"## {title} ({len(items)})", ""]
        lines += [f"- {item}" for item in items] or ["None."]
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Build pokedex.db from pinned raw data.")
    parser.add_argument("--raw", type=Path, default=ROOT / "data" / "raw", help="raw data directory (default: data/raw)")
    parser.add_argument(
        "--sources", type=Path, default=ROOT / "data" / "metadata" / "sources.json", help="sources.json (default: data/metadata/sources.json)"
    )
    parser.add_argument(
        "--out", type=Path, default=ROOT / "data" / "generated" / "pokedex.db", help="output database (default: data/generated/pokedex.db)"
    )
    args = parser.parse_args()

    sources = load_sources(args.sources)
    raw_errors = check_raw(sources, args.raw, BUILD_SOURCES)
    if raw_errors:
        for e in raw_errors:
            print(e, file=sys.stderr)
        return 1

    out: Path = args.out
    out.parent.mkdir(parents=True, exist_ok=True)
    part = out.with_name(out.name + ".part")
    part.unlink(missing_ok=True)

    conn = sqlite3.connect(part)
    try:
        conn.execute("PRAGMA page_size = 4096")
        conn.execute("PRAGMA journal_mode = OFF")
        conn.execute("PRAGMA synchronous = OFF")
        conn.executescript(SCHEMA.read_text(encoding="utf-8"))
        search_warnings = populate(conn, args.raw, sources)
        conn.commit()
        conn.execute(f"PRAGMA user_version = {SCHEMA_VERSION}")
        conn.execute("VACUUM")
    finally:
        conn.close()

    size, digest = part.stat().st_size, sha256_file(part)
    conn = sqlite3.connect(f"{part.resolve().as_uri()}?mode=ro", uri=True)
    try:
        rows = species_name_conflicts(conn, args.raw / "sindresorhus-pokemon")
        write_conflicts(out.parent / "conflicts.csv", rows)
        errors, warnings = checks.run_all(conn)
        warnings = search_warnings + warnings
        write_report(out.parent / "build-report.md", conn, len(rows), size, digest, errors, warnings)
    finally:
        conn.close()

    if errors:
        for e in errors:
            print(e, file=sys.stderr)
        print(f"build failed; database kept at {part}", file=sys.stderr)
        return 1
    os.replace(part, out)
    print(f"{out.name}: {size} bytes, sha256 {digest}, warnings: {len(warnings)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

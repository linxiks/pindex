"""Conflict log (D1-16): PokeAPI species zh-Hans / zh-Hant names vs sindresorhus/pokemon.

PokeAPI is always the adopted value. Showdown is compared separately by
tools/phase0/showdown_diff.py and is not part of the build.
"""

from __future__ import annotations

import csv
import json
import sqlite3
import unicodedata
from pathlib import Path

HEADER = ["field", "entity", "entity_id", "lang", "adopted_source", "adopted_value", "other_source", "other_value"]

# (lang, sindresorhus file); array index i is species id i + 1
SINDRESORHUS_FILES = (("zh-Hans", "zh-hans.json"), ("zh-Hant", "zh-hant.json"))


def _norm(s: str) -> str:
    # Same rule as tools/phase0/species_names_diff.py
    return unicodedata.normalize("NFC", s).strip()


def species_name_conflicts(conn: sqlite3.Connection, sr_dir: Path) -> list[list[str]]:
    rows = []
    for lang, filename in SINDRESORHUS_FILES:
        other = {i + 1: name for i, name in enumerate(json.loads((sr_dir / filename).read_text(encoding="utf-8")))}
        adopted = dict(
            conn.execute("SELECT entity_id, name FROM localized_name WHERE entity = 'species' AND lang = ?", (lang,))
        )
        for species_id in sorted(adopted.keys() | other.keys()):
            ours, theirs = adopted.get(species_id, ""), other.get(species_id, "")
            if _norm(ours) != _norm(theirs):
                source = "pokeapi" if species_id in adopted else "fallback"
                rows.append(["name", "species", str(species_id), lang, source, ours, "sindresorhus-pokemon", theirs])
    return rows


def write_conflicts(path: Path, rows: list[list[str]]) -> None:
    with open(path, "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f, lineterminator="\n")
        writer.writerow(HEADER)
        writer.writerows(rows)

#!/usr/bin/env python3
"""Run the pokedex.db integrity checks on an existing database.

Errors go to stderr and exit with code 1; warnings (known data gaps) go to stdout.

Run from the repository root: python3 tools/data-builder/verify.py
"""

from __future__ import annotations

import argparse
import sqlite3
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from builder import checks  # noqa: E402
from builder.raw import ROOT  # noqa: E402


def main() -> int:
    parser = argparse.ArgumentParser(description="Check pokedex.db integrity.")
    parser.add_argument(
        "--db", type=Path, default=ROOT / "data" / "generated" / "pokedex.db", help="database (default: data/generated/pokedex.db)"
    )
    args = parser.parse_args()
    if not args.db.is_file():
        print(f"missing database: {args.db}", file=sys.stderr)
        return 1

    conn = sqlite3.connect(f"{args.db.resolve().as_uri()}?mode=ro", uri=True)
    try:
        errors, warnings = checks.run_all(conn)
    finally:
        conn.close()
    for w in warnings:
        print(w)
    for e in errors:
        print(e, file=sys.stderr)
    print(f"errors: {len(errors)}, warnings: {len(warnings)}")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())

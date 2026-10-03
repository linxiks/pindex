"""Raw input access: pinned PokeAPI CSV files and data/metadata/sources.json."""

from __future__ import annotations

import csv
import json
from collections.abc import Iterator
from pathlib import Path

from fetch import sha256_file

ROOT = Path(__file__).resolve().parents[3]


class Raw:
    """CSV directory such as data/raw/pokeapi."""

    def __init__(self, root: Path) -> None:
        self.root = root

    def rows(self, name: str) -> Iterator[dict[str, str]]:
        with open(self.root / f"{name}.csv", encoding="utf-8", newline="") as f:
            yield from csv.DictReader(f)


def opt_int(s: str) -> int | None:
    return None if s == "" else int(s)


def opt_str(s: str) -> str | None:
    return None if s == "" else s


def load_sources(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def display_path(path: Path) -> str:
    try:
        return str(path.resolve().relative_to(ROOT))
    except ValueError:
        return str(path)


def check_raw(sources: dict, raw_root: Path, keys: tuple[str, ...]) -> list[str]:
    """Return one error per raw file of `keys` that is missing or differs from its pinned sha256."""
    errors = []
    for key in keys:
        for path, entry in sorted(sources["sources"][key]["files"].items()):
            local = raw_root / key / Path(path).name
            if not local.is_file():
                errors.append(f"missing raw file: {display_path(local)} (run python3 tools/data-builder/fetch.py)")
            elif entry is None:
                errors.append(f"sha256 not pinned in sources.json: {display_path(local)}")
            elif sha256_file(local) != entry["sha256"]:
                errors.append(f"sha256 mismatch: {display_path(local)}")
    return errors

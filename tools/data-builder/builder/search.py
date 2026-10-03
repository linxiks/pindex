"""search_index (D1-17)."""

from __future__ import annotations

import sqlite3
import unicodedata
from collections import defaultdict

from .importers import imported_ids, insert_sorted
from .lang import resolve
from .texts import ENTITY_TABLES

SEARCH_ENTITIES = ("species", "move", "ability", "item")

PRIORITY = {"zh-Hans": 0, "zh-Hant": 0, "en": 1, "ja": 2, "ja-Hrkt": 2}


def normalize_term(s: str) -> str:
    """NFKC (full width -> half width), lower case, drop all whitespace and '#'.

    The app normalizes user input with the same rule (Kotlin Normalizer.Form.NFKC + lowercase()).
    """
    s = unicodedata.normalize("NFKC", s).lower()
    return "".join(ch for ch in s if not ch.isspace() and ch != "#")


def build_search_index(conn: sqlite3.Connection) -> list[str]:
    """Fill search_index from localized_name; return warnings for entities without a display name."""
    names: dict[tuple[str, int], dict[str, str]] = defaultdict(dict)
    query = "SELECT entity, entity_id, lang, name FROM localized_name WHERE entity IN (?, ?, ?, ?)"
    for entity, entity_id, lang, name in conn.execute(query, SEARCH_ENTITIES):
        names[(entity, entity_id)][lang] = name

    warnings = []
    best: dict[tuple[str, str, int], tuple[str, int]] = {}
    for entity in SEARCH_ENTITIES:
        for entity_id in sorted(imported_ids(conn, ENTITY_TABLES[entity])):
            texts = names.get((entity, entity_id), {})
            display = resolve(texts)
            if display is None:
                warnings.append(f"search_index: {entity} {entity_id} has no zh-Hans/zh-Hant/en name, skipped")
                continue
            for lang, name in texts.items():
                term = normalize_term(name)
                if not term:
                    continue
                key = (term, entity, entity_id)
                priority = PRIORITY[lang]
                if key not in best or priority < best[key][1]:
                    best[key] = (display[0], priority)

    rows = [(term, entity, entity_id, display, priority) for (term, entity, entity_id), (display, priority) in best.items()]
    insert_sorted(conn, "search_index", ("term", "entity", "entity_id", "display", "priority"), rows, npk=3)
    return warnings

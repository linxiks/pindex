"""Integrity checks run after the build and by verify.py (D1-19 .. D1-21, project.md section 37).

Each check returns (errors, warnings). Errors fail the build; warnings are
known gaps of the data sources and only go into the build report.
"""

from __future__ import annotations

import json
import sqlite3
from collections import defaultdict

from .lang import LANG_CODES, resolve
from .search import SEARCH_ENTITIES
from .texts import ENTITY_TABLES, FLAVOR_TABLES

META_KEYS = ("build_date", "data_version", "schema_version", "source_versions")

Result = tuple[list[str], list[str]]


def _col(conn: sqlite3.Connection, sql: str, params: tuple = ()) -> list[str]:
    return [str(r[0]) for r in conn.execute(sql, params)]


def _add(out: list[str], group: str, desc: str, items: list[str], limit: int = 20) -> None:
    """Append one line `<group>: <desc> (<n>): a, b, ...` when items is non-empty."""
    if not items:
        return
    shown = ", ".join(items[:limit])
    more = ", ..." if len(items) > limit else ""
    out.append(f"{group}: {desc} ({len(items)}): {shown}{more}")


def _unresolved(conn: sqlite3.Connection, entity: str, table: str) -> list[str]:
    """Entities whose name cannot be resolved through zh-Hans -> zh-Hant -> en."""
    names: dict[int, dict[str, str]] = defaultdict(dict)
    for entity_id, lang, name in conn.execute("SELECT entity_id, lang, name FROM localized_name WHERE entity = ?", (entity,)):
        names[entity_id][lang] = name
    return [
        f"{i} {ident}"
        for i, ident in conn.execute(f"SELECT id, identifier FROM {table} ORDER BY id")
        if resolve(names.get(i, {})) is None
    ]


def _missing_zh_hans(conn: sqlite3.Connection, entity: str, table: str) -> list[str]:
    sql = f"""
        SELECT t.id || ' ' || t.identifier FROM {table} t
        WHERE NOT EXISTS (
            SELECT 1 FROM localized_name n WHERE n.entity = ? AND n.entity_id = t.id AND n.lang = 'zh-Hans'
        )
        ORDER BY t.id"""
    return _col(conn, sql, (entity,))


def check_pokemon(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    warnings: list[str] = []
    g = "pokemon"

    ids = [r[0] for r in conn.execute("SELECT id FROM pokemon_species ORDER BY id")]
    gap = next((n for n, species_id in enumerate(ids, 1) if species_id != n), None)
    if not ids:
        errors.append(f"{g}: no species")
    elif gap is not None:
        errors.append(f"{g}: species ids not contiguous at {gap}")
    (species_gen,) = conn.execute("SELECT MAX(generation_id) FROM pokemon_species").fetchone()
    (latest_gen,) = conn.execute("SELECT MAX(id) FROM generation").fetchone()
    if species_gen != latest_gen:
        errors.append(f"{g}: species reach generation {species_gen}, latest generation is {latest_gen}")

    _add(errors, g, "species missing zh-Hans name", _missing_zh_hans(conn, "species", "pokemon_species"))
    _add(errors, g, "pokemon species_id not found", _col(conn, """
        SELECT p.identifier FROM pokemon p
        WHERE NOT EXISTS (SELECT 1 FROM pokemon_species s WHERE s.id = p.species_id) ORDER BY p.id"""))
    _add(errors, g, "pokemon without 1-2 types in slots 1..n", _col(conn, """
        SELECT p.identifier FROM pokemon p LEFT JOIN pokemon_type t ON t.pokemon_id = p.id
        GROUP BY p.id
        HAVING NOT (COUNT(t.slot) BETWEEN 1 AND 2 AND MIN(t.slot) = 1 AND MAX(t.slot) = COUNT(t.slot))
        ORDER BY p.id"""))
    _add(errors, g, "pokemon without base stats 1..6", _col(conn, """
        SELECT p.identifier FROM pokemon p LEFT JOIN pokemon_stat s ON s.pokemon_id = p.id
        GROUP BY p.id
        HAVING NOT (COUNT(s.stat_id) = 6 AND MIN(s.stat_id) = 1 AND MAX(s.stat_id) = 6)
        ORDER BY p.id"""))
    _add(errors, g, "orphan forms (pokemon_id not found)", _col(conn, """
        SELECT f.id FROM pokemon_form f
        WHERE NOT EXISTS (SELECT 1 FROM pokemon p WHERE p.id = f.pokemon_id) ORDER BY f.id"""))
    _add(errors, g, "pokemon without forms", _col(conn, """
        SELECT p.identifier FROM pokemon p
        WHERE NOT EXISTS (SELECT 1 FROM pokemon_form f WHERE f.pokemon_id = p.id) ORDER BY p.id"""))

    _add(warnings, g, "pokemon without a default form", _col(conn, """
        SELECT p.identifier FROM pokemon p
        WHERE EXISTS (SELECT 1 FROM pokemon_form f WHERE f.pokemon_id = p.id)
          AND NOT EXISTS (SELECT 1 FROM pokemon_form f WHERE f.pokemon_id = p.id AND f.is_default = 1)
        ORDER BY p.id"""))
    _add(warnings, g, "species missing zh-Hans genus", _col(conn, """
        SELECT entity_id FROM localized_name
        WHERE entity = 'species' AND lang = 'zh-Hans' AND genus IS NULL ORDER BY entity_id"""))
    return errors, warnings


def check_moves(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    warnings: list[str] = []
    g = "moves"
    _add(errors, g, "id >= 10000", _col(conn, "SELECT id FROM move WHERE id >= 10000 ORDER BY id"))
    _add(errors, g, "no zh-Hans/zh-Hant/en name", _unresolved(conn, "move", "move"))
    _add(errors, g, "type not found", _col(conn, """
        SELECT m.id || ' ' || m.identifier FROM move m
        WHERE NOT EXISTS (SELECT 1 FROM type t WHERE t.id = m.type_id) ORDER BY m.id"""))
    _add(errors, g, "damage class not found", _col(conn, """
        SELECT m.id || ' ' || m.identifier FROM move m
        WHERE NOT EXISTS (SELECT 1 FROM move_damage_class d WHERE d.id = m.damage_class_id) ORDER BY m.id"""))
    _add(errors, g, "pp is NULL", _col(conn, "SELECT id || ' ' || identifier FROM move WHERE pp IS NULL ORDER BY id"))

    _add(warnings, g, "missing zh-Hans name", _missing_zh_hans(conn, "move", "move"))
    _add(warnings, g, "pokemon without learnset", _col(conn, """
        SELECT p.identifier FROM pokemon p
        WHERE NOT EXISTS (SELECT 1 FROM pokemon_move m WHERE m.pokemon_id = p.id) ORDER BY p.id"""), limit=10)
    return errors, warnings


def check_abilities(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    warnings: list[str] = []
    g = "abilities"
    _add(errors, g, "no zh-Hans/zh-Hant/en name", _unresolved(conn, "ability", "ability"))
    _add(errors, g, "is_hidden inconsistent with slot 3 (pokemon_id:slot)", _col(conn, """
        SELECT pokemon_id || ':' || slot FROM pokemon_ability
        WHERE (is_hidden = 1) <> (slot = 3) ORDER BY pokemon_id, slot"""))
    _add(errors, g, "ability not found (pokemon_id:slot)", _col(conn, """
        SELECT pa.pokemon_id || ':' || pa.slot FROM pokemon_ability pa
        WHERE NOT EXISTS (SELECT 1 FROM ability a WHERE a.id = pa.ability_id) ORDER BY pa.pokemon_id, pa.slot"""))

    _add(warnings, g, "missing zh-Hans name", _missing_zh_hans(conn, "ability", "ability"))
    _add(warnings, g, "not used by any pokemon", _col(conn, """
        SELECT a.id || ' ' || a.identifier FROM ability a
        WHERE NOT EXISTS (SELECT 1 FROM pokemon_ability pa WHERE pa.ability_id = a.id) ORDER BY a.id"""))
    _add(warnings, g, "pokemon without abilities", _col(conn, """
        SELECT p.identifier FROM pokemon p
        WHERE NOT EXISTS (SELECT 1 FROM pokemon_ability pa WHERE pa.pokemon_id = p.id) ORDER BY p.id"""))
    return errors, warnings


def check_foreign_keys(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    g = "foreign_keys"

    invalid: dict[tuple[str, int, str], int] = defaultdict(int)
    for table, _rowid, parent, fkid in conn.execute("PRAGMA foreign_key_check"):
        invalid[(table, fkid, parent)] += 1
    for (table, fkid, parent), n in sorted(invalid.items()):
        column = next(r[3] for r in conn.execute(f"PRAGMA foreign_key_list({table})") if r[0] == fkid)
        errors.append(f"{g}: {table}.{column} -> {parent}: {n} invalid references")

    polymorphic = (("localized_name", ENTITY_TABLES), ("search_index", {e: ENTITY_TABLES[e] for e in SEARCH_ENTITIES}))
    for table, targets in polymorphic:
        _add(errors, g, f"{table} unknown entity", [e for e in _col(conn, f"SELECT DISTINCT entity FROM {table} ORDER BY entity") if e not in targets])
        for entity, target in targets.items():
            (n,) = conn.execute(f"""
                SELECT COUNT(*) FROM {table} x
                WHERE x.entity = ? AND NOT EXISTS (SELECT 1 FROM {target} t WHERE t.id = x.entity_id)""", (entity,)).fetchone()
            if n:
                errors.append(f"{g}: {table} {entity} -> {target}: {n} invalid references")

    codes = set(LANG_CODES.values())
    for table in ("localized_name",) + tuple(t for t, *_ in FLAVOR_TABLES):
        _add(errors, g, f"{table} unknown lang", [lang for lang in _col(conn, f"SELECT DISTINCT lang FROM {table} ORDER BY lang") if lang not in codes])
    return errors, []


def check_meta(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    g = "meta"
    meta = dict(conn.execute("SELECT key, value FROM meta"))
    _add(errors, g, "missing keys", [k for k in META_KEYS if k not in meta])
    if "source_versions" in meta:
        try:
            ok = isinstance(json.loads(meta["source_versions"]), dict)
        except json.JSONDecodeError:
            ok = False
        if not ok:
            errors.append(f"{g}: source_versions is not a JSON object")
    (user_version,) = conn.execute("PRAGMA user_version").fetchone()
    if meta.get("schema_version") != str(user_version):
        errors.append(f"{g}: PRAGMA user_version {user_version} != schema_version {meta.get('schema_version')}")
    return errors, []


CHECKS = (check_pokemon, check_moves, check_abilities, check_foreign_keys, check_meta)


def run_all(conn: sqlite3.Connection) -> Result:
    errors: list[str] = []
    warnings: list[str] = []
    for check in CHECKS:
        e, w = check(conn)
        errors += e
        warnings += w
    return errors, warnings

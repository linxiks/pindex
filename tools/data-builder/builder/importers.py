"""Entity and relation tables imported from PokeAPI CSV (D1-3 .. D1-12).

Every row is converted in memory (numbers -> int, "" -> NULL) and inserted in
primary-key order so two builds produce identical files. Duplicate primary keys
are not deduplicated: sqlite3.IntegrityError aborts the build.
"""

from __future__ import annotations

import json
import sqlite3
from collections.abc import Callable, Iterable

from .raw import Raw, opt_int, opt_str

# (CSV column, table column, converter)
Spec = tuple[tuple[str, str, Callable[[str], object]], ...]

ID_IDENTIFIER: Spec = (("id", "id", int), ("identifier", "identifier", str))


def insert_sorted(conn: sqlite3.Connection, table: str, cols: Iterable[str], rows: list[tuple], npk: int = 1) -> None:
    """Insert rows sorted by their first `npk` values (the primary key columns)."""
    cols = tuple(cols)
    rows.sort(key=lambda r: r[:npk])
    placeholders = ", ".join("?" * len(cols))
    conn.executemany(f"INSERT INTO {table} ({', '.join(cols)}) VALUES ({placeholders})", rows)


def _copy(
    conn: sqlite3.Connection,
    raw: Raw,
    table: str,
    csv_name: str,
    spec: Spec,
    npk: int = 1,
    keep: Callable[[dict[str, str]], bool] = lambda row: True,
) -> None:
    rows = [tuple(conv(row[src]) for src, _, conv in spec) for row in raw.rows(csv_name) if keep(row)]
    insert_sorted(conn, table, (dst for _, dst, _ in spec), rows, npk)


def imported_ids(conn: sqlite3.Connection, table: str) -> set[int]:
    return {r[0] for r in conn.execute(f"SELECT id FROM {table}")}


def _below_10000(row: dict[str, str]) -> bool:
    return int(row["id"]) < 10000


def _level(s: str) -> int:
    return 0 if s == "" else int(s)


def import_generation(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "generation", "generations", ID_IDENTIFIER)


def import_version_group(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("id", "id", int), ("generation_id", "generation_id", int), ("identifier", "identifier", str), ("order", "sort_order", int))
    _copy(conn, raw, "version_group", "version_groups", spec)


def import_version(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("id", "id", int), ("version_group_id", "version_group_id", int), ("identifier", "identifier", str))
    _copy(conn, raw, "version", "versions", spec)


def import_type(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = ID_IDENTIFIER + (("generation_id", "generation_id", int),)
    _copy(conn, raw, "type", "types", spec, keep=_below_10000)


def import_stat(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "stat", "stats", ID_IDENTIFIER)


def import_move_method(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "move_method", "pokemon_move_methods", ID_IDENTIFIER)


def import_move_damage_class(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "move_damage_class", "move_damage_classes", ID_IDENTIFIER)


def import_growth_rate(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "growth_rate", "growth_rates", ID_IDENTIFIER)


def import_evolution_trigger(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "evolution_trigger", "evolution_triggers", ID_IDENTIFIER)


def import_region(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "region", "regions", ID_IDENTIFIER)


def import_location(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("id", "id", int), ("region_id", "region_id", opt_int), ("identifier", "identifier", str))
    _copy(conn, raw, "location", "locations", spec)


def import_type_efficacy(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (
        ("damage_type_id", "attack_type_id", int),
        ("target_type_id", "defend_type_id", int),
        ("damage_factor", "factor", int),
    )
    _copy(conn, raw, "type_efficacy", "type_efficacy", spec, npk=2)


def import_evolution_chain(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("id", "id", int), ("baby_trigger_item_id", "baby_trigger_item_id", opt_int))
    _copy(conn, raw, "evolution_chain", "evolution_chains", spec)


def import_pokemon_species(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (
        ("id", "id", int),
        ("identifier", "identifier", str),
        ("generation_id", "generation_id", int),
        ("evolution_chain_id", "evolution_chain_id", int),
        ("evolves_from_species_id", "evolves_from_species_id", opt_int),
        ("gender_rate", "gender_rate", int),
        ("capture_rate", "capture_rate", int),
        ("base_happiness", "base_happiness", int),
        ("hatch_counter", "hatch_counter", int),
        ("growth_rate_id", "growth_rate_id", int),
        ("is_baby", "is_baby", int),
        ("is_legendary", "is_legendary", int),
        ("is_mythical", "is_mythical", int),
        ("order", "sort_order", int),
    )
    _copy(conn, raw, "pokemon_species", "pokemon_species", spec)


def import_pokemon(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (
        ("id", "id", int),
        ("species_id", "species_id", int),
        ("identifier", "identifier", str),
        ("height", "height", int),
        ("weight", "weight", int),
        ("base_experience", "base_experience", opt_int),
        ("is_default", "is_default", int),
        ("order", "sort_order", opt_int),
    )
    _copy(conn, raw, "pokemon", "pokemon", spec)


def import_pokemon_form(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (
        ("id", "id", int),
        ("pokemon_id", "pokemon_id", int),
        ("form_identifier", "form_identifier", opt_str),
        ("is_default", "is_default", int),
        ("is_battle_only", "is_battle_only", int),
        ("is_mega", "is_mega", int),
        ("introduced_in_version_group_id", "introduced_in_version_group_id", int),
        ("order", "sort_order", int),
    )
    _copy(conn, raw, "pokemon_form", "pokemon_forms", spec)


def import_pokemon_type(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("pokemon_id", "pokemon_id", int), ("slot", "slot", int), ("type_id", "type_id", int))
    _copy(conn, raw, "pokemon_type", "pokemon_types", spec, npk=2)


def import_pokemon_stat(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("pokemon_id", "pokemon_id", int), ("stat_id", "stat_id", int), ("base_stat", "base_value", int))
    _copy(conn, raw, "pokemon_stat", "pokemon_stats", spec, npk=2)


def import_ability(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = ID_IDENTIFIER + (("generation_id", "generation_id", int),)
    _copy(conn, raw, "ability", "abilities", spec, keep=lambda row: row["is_main_series"] == "1")


def import_pokemon_ability(conn: sqlite3.Connection, raw: Raw) -> None:
    abilities = imported_ids(conn, "ability")
    spec = (
        ("pokemon_id", "pokemon_id", int),
        ("slot", "slot", int),
        ("ability_id", "ability_id", int),
        ("is_hidden", "is_hidden", int),
    )
    _copy(conn, raw, "pokemon_ability", "pokemon_abilities", spec, npk=2, keep=lambda row: int(row["ability_id"]) in abilities)


def import_move(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (
        ("id", "id", int),
        ("identifier", "identifier", str),
        ("type_id", "type_id", int),
        ("damage_class_id", "damage_class_id", int),
        ("power", "power", opt_int),
        ("accuracy", "accuracy", opt_int),
        ("pp", "pp", int),
        ("priority", "priority", int),
        ("generation_id", "generation_id", int),
    )
    _copy(conn, raw, "move", "moves", spec, keep=_below_10000)


def import_pokemon_move(conn: sqlite3.Connection, raw: Raw) -> None:
    moves = imported_ids(conn, "move")
    spec = (
        ("pokemon_id", "pokemon_id", int),
        ("version_group_id", "version_group_id", int),
        ("move_id", "move_id", int),
        ("pokemon_move_method_id", "method_id", int),
        ("level", "level", _level),
        ("order", "sort_order", opt_int),
        ("mastery", "mastery", opt_int),
    )
    _copy(conn, raw, "pokemon_move", "pokemon_moves", spec, npk=5, keep=lambda row: int(row["move_id"]) in moves)


def import_item(conn: sqlite3.Connection, raw: Raw) -> None:
    categories = {int(r["id"]): r["identifier"] for r in raw.rows("item_categories")}
    rows = []
    for r in raw.rows("items"):
        item_id, category_id = int(r["id"]), int(r["category_id"])
        if category_id not in categories:
            raise ValueError(f"item {item_id}: unknown category {category_id}")
        rows.append((item_id, r["identifier"], categories[category_id]))
    insert_sorted(conn, "item", ("id", "identifier", "category_identifier"), rows)


EVOLUTION_SPEC: Spec = (
    ("id", "id", int),
    ("evolved_species_id", "evolved_species_id", int),
    ("evolved_pokemon_form_id", "evolved_pokemon_form_id", opt_int),
    ("version_group_id", "version_group_id", int),
    ("is_default", "is_default", int),
    ("evolution_trigger_id", "trigger_id", int),
    ("minimum_level", "min_level", opt_int),
    ("trigger_item_id", "trigger_item_id", opt_int),
    ("held_item_id", "held_item_id", opt_int),
    ("known_move_id", "known_move_id", opt_int),
    ("known_move_type_id", "known_move_type_id", opt_int),
    ("gender_id", "gender_id", opt_int),
    ("time_of_day", "time_of_day", opt_str),
    ("minimum_happiness", "min_happiness", opt_int),
    ("minimum_affection", "min_affection", opt_int),
    ("minimum_beauty", "min_beauty", opt_int),
    ("location_id", "location_id", opt_int),
    ("region_id", "region_id", opt_int),
    ("trade_species_id", "trade_species_id", opt_int),
)


def raw_conditions(row: dict[str, str]) -> str:
    """All non-empty columns of the source row as compact JSON, values kept as CSV strings."""
    return json.dumps({k: v for k, v in row.items() if v != ""}, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def import_evolution(conn: sqlite3.Connection, raw: Raw) -> None:
    rows = [
        tuple(conv(row[src]) for src, _, conv in EVOLUTION_SPEC) + (raw_conditions(row),)
        for row in raw.rows("pokemon_evolution")
    ]
    insert_sorted(conn, "evolution", [dst for _, dst, _ in EVOLUTION_SPEC] + ["raw_conditions"], rows)


def import_nature(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = ID_IDENTIFIER + (("increased_stat_id", "increased_stat_id", int), ("decreased_stat_id", "decreased_stat_id", int))
    _copy(conn, raw, "nature", "natures", spec)


def import_egg_group(conn: sqlite3.Connection, raw: Raw) -> None:
    _copy(conn, raw, "egg_group", "egg_groups", ID_IDENTIFIER)


def import_species_egg_group(conn: sqlite3.Connection, raw: Raw) -> None:
    spec = (("species_id", "species_id", int), ("egg_group_id", "egg_group_id", int))
    _copy(conn, raw, "species_egg_group", "pokemon_egg_groups", spec, npk=2)


# Build order: filters use imported_ids of tables imported earlier.
IMPORTERS = (
    import_generation,
    import_version_group,
    import_version,
    import_type,
    import_stat,
    import_move_method,
    import_move_damage_class,
    import_growth_rate,
    import_evolution_trigger,
    import_region,
    import_location,
    import_type_efficacy,
    import_evolution_chain,
    import_pokemon_species,
    import_pokemon,
    import_pokemon_form,
    import_pokemon_type,
    import_pokemon_stat,
    import_ability,
    import_pokemon_ability,
    import_move,
    import_pokemon_move,
    import_item,
    import_evolution,
    import_nature,
    import_egg_group,
    import_species_egg_group,
)

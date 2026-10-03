"""localized_name and the three flavor_text tables (D1-13, D1-14).

Only real text is stored: `lang` is the language of the text, no fallback
copies. Rows of entities outside the import scope are dropped.
"""

from __future__ import annotations

import sqlite3

from .importers import imported_ids, insert_sorted
from .raw import Raw, opt_str

SOURCE = "pokeapi"

# (entity, CSV, entity id column, name column, entity table)
NAME_TABLES = (
    ("species", "pokemon_species_names", "pokemon_species_id", "name", "pokemon_species"),
    ("pokemon_form", "pokemon_form_names", "pokemon_form_id", "form_name", "pokemon_form"),
    ("move", "move_names", "move_id", "name", "move"),
    ("ability", "ability_names", "ability_id", "name", "ability"),
    ("item", "item_names", "item_id", "name", "item"),
    ("type", "type_names", "type_id", "name", "type"),
    ("nature", "nature_names", "nature_id", "name", "nature"),
    ("egg_group", "egg_group_prose", "egg_group_id", "name", "egg_group"),
    ("stat", "stat_names", "stat_id", "name", "stat"),
    ("version", "version_names", "version_id", "name", "version"),
    ("generation", "generation_names", "generation_id", "name", "generation"),
    ("growth_rate", "growth_rate_prose", "growth_rate_id", "name", "growth_rate"),
    ("move_damage_class", "move_damage_class_prose", "move_damage_class_id", "name", "move_damage_class"),
    ("location", "location_names", "location_id", "name", "location"),
    ("region", "region_names", "region_id", "name", "region"),
)

ENTITY_TABLES = {entity: table for entity, *_, table in NAME_TABLES}

# (table and CSV name, entity id column, version column, entity table); CSV and table share column names.
FLAVOR_TABLES = (
    ("species_flavor_text", "pokemon_species_flavor_text", "species_id", "version_id", "pokemon_species"),
    ("move_flavor_text", "move_flavor_text", "move_id", "version_group_id", "move"),
    ("ability_flavor_text", "ability_flavor_text", "ability_id", "version_group_id", "ability"),
)


def import_localized_names(conn: sqlite3.Connection, raw: Raw, langs: dict[int, str]) -> None:
    rows = []
    for entity, csv_name, id_col, name_col, table in NAME_TABLES:
        ids = imported_ids(conn, table)
        for r in raw.rows(csv_name):
            lang = langs.get(int(r["local_language_id"]))
            entity_id, name = int(r[id_col]), r[name_col]
            if lang is None or name == "" or entity_id not in ids:
                continue
            genus = opt_str(r["genus"]) if entity == "species" else None
            rows.append((entity, entity_id, lang, name, genus, SOURCE))
    insert_sorted(conn, "localized_name", ("entity", "entity_id", "lang", "name", "genus", "source"), rows, npk=3)


def import_flavor_texts(conn: sqlite3.Connection, raw: Raw, langs: dict[int, str]) -> None:
    for table, csv_name, id_col, version_col, entity_table in FLAVOR_TABLES:
        ids = imported_ids(conn, entity_table)
        rows = []
        for r in raw.rows(csv_name):
            lang = langs.get(int(r["language_id"]))
            entity_id = int(r[id_col])
            if lang is None or entity_id not in ids:
                continue
            rows.append((entity_id, int(r[version_col]), lang, r["flavor_text"], SOURCE))
        insert_sorted(conn, table, (id_col, version_col, "lang", "text", "source"), rows, npk=3)

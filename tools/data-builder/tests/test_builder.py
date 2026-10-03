"""Unit tests for the pokedex.db builder (D1-22).

Run from the repository root: python3 -m unittest discover -s tools/data-builder/tests -v
Fixtures are minimal CSV files written per test; data/raw/ is not used.
"""

from __future__ import annotations

import csv
import json
import sqlite3
import sys
import tempfile
import unittest
from pathlib import Path

BUILDER_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BUILDER_DIR))

from builder.checks import check_pokemon  # noqa: E402
from builder.importers import (  # noqa: E402
    EVOLUTION_SPEC,
    import_ability,
    import_evolution,
    import_move,
    import_pokemon_form,
    import_pokemon_move,
    import_type,
)
from builder.lang import lang_map, resolve  # noqa: E402
from builder.raw import Raw  # noqa: E402
from builder.search import build_search_index, normalize_term  # noqa: E402
from builder.texts import NAME_TABLES, import_localized_names  # noqa: E402

SCHEMA = (BUILDER_DIR / "schema.sql").read_text(encoding="utf-8")

LANGUAGES = [["1", "ja-hrkt"], ["4", "zh-hant"], ["7", "es"], ["9", "en"], ["11", "ja"], ["12", "zh-hans"]]
MOVE_HEADER = ["id", "identifier", "generation_id", "type_id", "power", "pp", "accuracy", "priority", "damage_class_id"]
FORM_HEADER = [
    "id", "identifier", "form_identifier", "pokemon_id", "introduced_in_version_group_id",
    "is_default", "is_battle_only", "is_mega", "form_order", "order",
]


def write_csv(directory: Path, name: str, header: list[str], rows: list[list[str]]) -> None:
    with open(directory / f"{name}.csv", "w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f, lineterminator="\n")
        writer.writerow(header)
        writer.writerows(rows)


class BuilderTest(unittest.TestCase):
    def setUp(self) -> None:
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        self.dir = Path(tmp.name)
        self.raw = Raw(self.dir)
        self.conn = sqlite3.connect(":memory:")
        self.addCleanup(self.conn.close)
        self.conn.executescript(SCHEMA)

    def write_name_tables(self, rows: dict[str, list[list[str]]]) -> None:
        """Write every name CSV import_localized_names reads; tables not in `rows` get only a header."""
        for entity, csv_name, id_col, name_col, _ in NAME_TABLES:
            header = [id_col, "local_language_id", name_col] + (["genus"] if entity == "species" else [])
            write_csv(self.dir, csv_name, header, rows.get(csv_name, []))

    def insert_move(self, move_id: int) -> None:
        self.conn.execute("INSERT INTO move VALUES (?, 'm', 1, 1, NULL, NULL, 10, 0, 1)", (move_id,))

    def test_resolve_fallback_order(self) -> None:
        self.assertEqual(resolve({"zh-Hans": "皮卡丘", "zh-Hant": "皮卡丘", "en": "Pikachu"}), ("皮卡丘", "zh-Hans"))
        self.assertEqual(resolve({"zh-Hant": "噴火龍", "en": "Charizard", "ja": "リザードン"}), ("噴火龍", "zh-Hant"))
        self.assertEqual(resolve({"en": "Eelevate", "ja": "x"}), ("Eelevate", "en"))
        self.assertIsNone(resolve({"ja": "リザードン", "ja-Hrkt": "リザードン"}))
        self.assertIsNone(resolve({}))

    def test_normalize_term(self) -> None:
        self.assertEqual(normalize_term("  Ｐｉｋａｃｈｕ "), "pikachu")
        self.assertEqual(normalize_term("多边兽２型"), "多边兽2型")
        self.assertEqual(normalize_term("#025"), "025")
        self.assertEqual(normalize_term("Mr. Mime"), "mr.mime")

    def test_pokemon_move_empty_level_and_order(self) -> None:
        self.insert_move(33)
        header = ["pokemon_id", "version_group_id", "move_id", "pokemon_move_method_id", "level", "order", "mastery"]
        write_csv(self.dir, "pokemon_moves", header, [["1", "1", "33", "1", "", "", ""]])
        import_pokemon_move(self.conn, self.raw)
        self.assertEqual(
            self.conn.execute("SELECT level, sort_order, mastery FROM pokemon_move").fetchall(), [(0, None, None)]
        )

    def test_pokemon_move_empty_level_collides_with_zero(self) -> None:
        self.insert_move(33)
        header = ["pokemon_id", "version_group_id", "move_id", "pokemon_move_method_id", "level", "order", "mastery"]
        write_csv(self.dir, "pokemon_moves", header, [["1", "1", "33", "1", "", "", ""], ["1", "1", "33", "1", "0", "", ""]])
        with self.assertRaises(sqlite3.IntegrityError):
            import_pokemon_move(self.conn, self.raw)

    def test_pokemon_without_default_form_is_warning(self) -> None:
        self.conn.execute("INSERT INTO pokemon VALUES (10264, 1007, 'koraidon-limited-build', 35, 3030, NULL, 0, NULL)")
        write_csv(self.dir, "pokemon_forms", FORM_HEADER, [
            ["10437", "a", "limited-build", "10264", "25", "0", "1", "0", "1", "1"],
            ["10438", "b", "other-build", "10264", "25", "0", "1", "0", "2", "2"],
        ])
        import_pokemon_form(self.conn, self.raw)
        self.assertEqual(self.conn.execute("SELECT COUNT(*) FROM pokemon_form").fetchone(), (2,))
        errors, warnings = check_pokemon(self.conn)
        self.assertTrue(any("without a default form" in w and "koraidon-limited-build" in w for w in warnings), warnings)
        self.assertFalse(any("form" in e for e in errors), errors)

    def test_orphan_form_is_error(self) -> None:
        write_csv(self.dir, "pokemon_forms", FORM_HEADER, [["20000", "x", "", "999", "1", "1", "0", "0", "1", "1"]])
        import_pokemon_form(self.conn, self.raw)
        errors, _ = check_pokemon(self.conn)
        self.assertTrue(any("orphan forms" in e and "20000" in e for e in errors), errors)

    def test_evolution_raw_conditions_keeps_all_non_empty_columns(self) -> None:
        header = [src for src, _, _ in EVOLUTION_SPEC] + ["party_species_id", "needs_overworld_rain", "condition_expression"]
        values = {src: "" for src in header}
        values.update({
            "id": "7", "evolved_species_id": "3", "version_group_id": "1", "is_default": "1",
            "evolution_trigger_id": "1", "minimum_level": "32", "time_of_day": "night",
            "party_species_id": "223", "needs_overworld_rain": "0", "condition_expression": "a, b",
        })
        row = [values[c] for c in header]
        write_csv(self.dir, "pokemon_evolution", header, [row])
        import_evolution(self.conn, self.raw)
        min_level, time_of_day, held, raw_json = self.conn.execute(
            "SELECT min_level, time_of_day, held_item_id, raw_conditions FROM evolution WHERE id = 7"
        ).fetchone()
        self.assertEqual((min_level, time_of_day, held), (32, "night", None))
        self.assertEqual(json.loads(raw_json), {k: v for k, v in values.items() if v != ""})

    def test_import_scope_excludes_shadow_moves_unknown_types_and_non_main_abilities(self) -> None:
        write_csv(self.dir, "languages", ["id", "identifier"], LANGUAGES)
        write_csv(self.dir, "types", ["id", "identifier", "generation_id", "damage_class_id"], [
            ["1", "normal", "1", "2"], ["10001", "unknown", "2", ""],
        ])
        write_csv(self.dir, "moves", MOVE_HEADER, [
            ["1", "pound", "1", "1", "40", "35", "100", "0", "2"],
            ["10001", "shadow-rush", "3", "10002", "55", "", "100", "0", "2"],
        ])
        write_csv(self.dir, "abilities", ["id", "identifier", "generation_id", "is_main_series"], [
            ["1", "stench", "3", "1"], ["10001", "mountaineer", "4", "0"],
        ])
        self.write_name_tables({
            "type_names": [["1", "12", "一般"], ["10001", "12", "？？？"]],
            "move_names": [["1", "12", "拍击"], ["10001", "9", "Shadow Rush"]],
            "ability_names": [["1", "12", "恶臭"], ["10001", "9", "Mountaineer"]],
        })
        import_type(self.conn, self.raw)
        import_move(self.conn, self.raw)
        import_ability(self.conn, self.raw)
        import_localized_names(self.conn, self.raw, lang_map(self.raw))
        for table in ("type", "move", "ability"):
            self.assertEqual(self.conn.execute(f"SELECT id FROM {table}").fetchall(), [(1,)], table)
        self.assertEqual(
            self.conn.execute("SELECT entity, entity_id, lang, name FROM localized_name ORDER BY entity").fetchall(),
            [("ability", 1, "zh-Hans", "恶臭"), ("move", 1, "zh-Hans", "拍击"), ("type", 1, "zh-Hans", "一般")],
        )

    def test_search_index_dedup_and_unnamed_entity(self) -> None:
        self.conn.execute("INSERT INTO pokemon_species VALUES (25, 'pikachu', 1, 10, 172, 4, 190, 50, 10, 2, 0, 0, 0, 35)")
        self.conn.execute("INSERT INTO item VALUES (2278, 'hopo-berry', 'baking-only')")
        self.conn.executemany("INSERT INTO localized_name VALUES (?, ?, ?, ?, NULL, 'pokeapi')", [
            ("species", 25, "zh-Hans", "皮卡丘"),
            ("species", 25, "zh-Hant", "皮卡丘"),
            ("species", 25, "en", "Pikachu"),
            ("item", 2278, "ja", "ホopo"),
        ])
        warnings = build_search_index(self.conn)
        self.assertEqual(
            self.conn.execute("SELECT term, entity, entity_id, display, priority FROM search_index ORDER BY priority").fetchall(),
            [("皮卡丘", "species", 25, "皮卡丘", 0), ("pikachu", "species", 25, "皮卡丘", 1)],
        )
        self.assertEqual(len(warnings), 1)
        self.assertIn("item 2278", warnings[0])


if __name__ == "__main__":
    unittest.main()

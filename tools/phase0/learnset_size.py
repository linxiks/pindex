#!/usr/bin/env python3
"""D0-7: measure SQLite size of pokemon_move under two import scopes.

A = every version group; B = per pokemon, only its latest (max version_groups.order)
version group with data. Output: data/reports/learnset-size.md
"""

from __future__ import annotations

import sqlite3
import sys
import tempfile
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from common import read_csv, table, write_report  # noqa: E402

SCHEMA = """
CREATE TABLE pokemon_move (
  pokemon_id INTEGER NOT NULL,
  version_group_id INTEGER NOT NULL,
  move_id INTEGER NOT NULL,
  method_id INTEGER NOT NULL,
  level INTEGER NOT NULL,
  sort_order INTEGER,
  PRIMARY KEY (pokemon_id, version_group_id, move_id, method_id, level)
) WITHOUT ROWID;
"""


def to_row(r: dict) -> tuple:
    return (
        int(r["pokemon_id"]),
        int(r["version_group_id"]),
        int(r["move_id"]),
        int(r["pokemon_move_method_id"]),
        int(r["level"] or 0),
        int(r["order"]) if r["order"] else None,
    )


def measure(rows: list[tuple], tmp: Path, name: str) -> tuple[int, int, int]:
    db = tmp / f"{name}.db"
    con = sqlite3.connect(db)
    con.executescript(SCHEMA)
    con.executemany("INSERT OR IGNORE INTO pokemon_move VALUES (?,?,?,?,?,?)", rows)
    con.commit()
    stored = con.execute("SELECT count(*) FROM pokemon_move").fetchone()[0]
    con.execute("VACUUM")
    no_index = db.stat().st_size
    con.execute("CREATE INDEX pokemon_move_move ON pokemon_move(move_id, version_group_id)")
    con.commit()
    con.execute("VACUUM")
    with_index = db.stat().st_size
    con.close()
    return stored, no_index, with_index


def mb(n: int) -> str:
    return f"{n / 1024 / 1024:.2f}"


def main() -> int:
    raw = read_csv("pokemon_moves")
    rows = [to_row(r) for r in raw]

    keys = Counter(r[:5] for r in rows)
    dups = [(k, c) for k, c in keys.items() if c > 1]
    dups.sort()

    vg_order = {int(v["id"]): int(v["order"]) for v in read_csv("version_groups")}
    latest: dict[int, int] = {}
    for r in rows:
        pid, vg = r[0], r[1]
        if pid not in latest or vg_order[vg] > vg_order[latest[pid]]:
            latest[pid] = vg
    rows_b = [r for r in rows if latest[r[0]] == r[1]]

    with tempfile.TemporaryDirectory(prefix="pindex-d0-7-") as td:
        tmp = Path(td)
        a = measure(rows, tmp, "a")
        b = measure(rows_b, tmp, "b")

    lines = [
        f"`pokemon_moves.csv` 总行数：{len(rows)}；涉及宝可梦：{len(latest)}。",
        "",
        f"五列键 (pokemon_id, version_group_id, move_id, pokemon_move_method_id, level；level 空值按 0) 重复组数：{len(dups)}"
        + (f"，多出行数：{sum(c - 1 for _, c in dups)}。" if dups else "。"),
        "",
    ]
    if dups:
        lines += ["前 20 组：", ""]
        lines += table(["pokemon_id", "version_group_id", "move_id", "method_id", "level", "行数"],
                       [[*k, c] for k, c in dups[:20]])
        lines.append("")

    lines += [
        "## 体积（SQLite WITHOUT ROWID，主键为五列键，INSERT OR IGNORE 后 VACUUM）",
        "",
        "- 方案 A：全部版本组",
        "- 方案 B：每个宝可梦只保留其有数据的版本组中 `version_groups.order` 最大的一个",
        "",
    ]
    lines += table(
        ["方案", "CSV 行数", "入库行数", "无索引 字节", "无索引 MB", "加 (move_id, version_group_id) 索引 字节", "加索引 MB"],
        [
            ["A", len(rows), a[0], a[1], mb(a[1]), a[2], mb(a[2])],
            ["B", len(rows_b), b[0], b[1], mb(b[1]), b[2], mb(b[2])],
        ],
    )
    path = write_report("learnset-size", "D0-7 学习面体积", ("pokeapi",), lines)
    print(path)
    print(f"A={a} B={b} dups={len(dups)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

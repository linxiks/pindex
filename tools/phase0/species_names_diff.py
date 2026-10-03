#!/usr/bin/env python3
"""D0-5: compare sindresorhus/pokemon zh names with PokeAPI pokemon_species_names.

PokeAPI is authoritative; sindresorhus is a verification source only.
Output: data/reports/species-name-diff.md
"""

from __future__ import annotations

import json
import sys
import unicodedata
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from common import RAW, lang_ids, read_csv, table, write_report  # noqa: E402


def norm(s: str | None) -> str:
    return unicodedata.normalize("NFC", s or "").strip()


def main() -> int:
    langs = lang_ids()
    species = {int(r["id"]): r["identifier"] for r in read_csv("pokemon_species")}
    names: dict[str, dict[int, str]] = {"zh-hans": {}, "zh-hant": {}}
    by_id = {langs[k]: k for k in names}
    for r in read_csv("pokemon_species_names"):
        lang = by_id.get(int(r["local_language_id"]))
        if lang and norm(r["name"]):
            names[lang][int(r["pokemon_species_id"])] = r["name"]

    lines: list[str] = [
        "PokeAPI 为权威来源（开发计划第二节）；sindresorhus 仅作校验源，不写入数据库。",
        "比较前两边均做 Unicode NFC 规范化并去除首尾空白；表中显示原文。",
        "",
    ]
    total_diff = 0
    for lang, fname in (("zh-hans", "zh-hans.json"), ("zh-hant", "zh-hant.json")):
        arr = json.loads((RAW / "sindresorhus-pokemon" / fname).read_text(encoding="utf-8"))
        sr = {i + 1: v for i, v in enumerate(arr)}
        pa = names[lang]
        lines += [f"## {lang}", "", f"- PokeAPI species 行数：{len(species)}；PokeAPI {lang} 名称：{len(pa)}；sindresorhus 条目：{len(sr)}"]
        if len(sr) != len(species):
            only_sr = sorted(set(sr) - set(species))
            only_pa = sorted(set(species) - set(sr))
            if only_sr:
                lines.append(f"- 仅 sindresorhus 有的 id：{only_sr[0]}–{only_sr[-1]}（{len(only_sr)} 个）")
            if only_pa:
                lines.append(f"- sindresorhus 缺少的 id：{only_pa[0]}–{only_pa[-1]}（{len(only_pa)} 个）")
        rows = []
        for sid in sorted(set(species) | set(sr)):
            p, s = pa.get(sid), sr.get(sid)
            if norm(p) == norm(s):
                continue
            if norm(p):
                adopt, why = p, "PokeAPI 为权威来源（开发计划第二节）"
            else:
                adopt, why = "回退 zh-Hant → en", "PokeAPI 缺失；sindresorhus 仅作校验源，不写入"
            rows.append([sid, species.get(sid, "—"), p or "—", s or "—", adopt, why])
        total_diff += len(rows)
        lines.append(f"- 差异条目：{len(rows)}")
        lines.append("")
        if rows:
            lines += table(["id", "identifier", "PokeAPI", "sindresorhus", "采用", "理由"], rows)
        else:
            lines.append("两边完全一致。")
        lines.append("")

    path = write_report("species-name-diff", "D0-5 物种中文名对比（PokeAPI vs sindresorhus/pokemon）",
                        ("pokeapi", "sindresorhus-pokemon"), lines)
    print(f"{path} diffs={total_diff}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

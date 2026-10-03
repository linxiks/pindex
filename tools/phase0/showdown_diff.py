#!/usr/bin/env python3
"""D0-6: compare Showdown typechart and gen-9 learnsets with PokeAPI.

Input: data/normalized/showdown/*.json (from showdown_dump.mjs) and PokeAPI CSVs.
Output: data/reports/showdown-diff.md. Reports differences only; changes no data.
"""

from __future__ import annotations

import json
import re
import sys
from collections import defaultdict

from common import NORMALIZED, read_csv, table, write_report

SV_GROUPS = ("scarlet-violet", "the-teal-mask", "the-indigo-disk")
METHODS = {"level-up": "L", "machine": "M", "tutor": "T", "egg": "E"}
SD_VALUE = {0: 100, 1: 200, 2: 50, 3: 0}
N_DEFAULT, N_FORMS = 20, 10


def sd_id(s: str) -> str:
    return re.sub("[^a-z0-9]", "", s.lower())


def load(name: str) -> dict:
    return json.loads((NORMALIZED / "showdown" / f"{name}.json").read_text(encoding="utf-8"))


def pick(items: list, k: int) -> list:
    n = len(items)
    if n <= k:
        return list(items)
    return [items[round(i * (n - 1) / (k - 1))] for i in range(k)]


def typechart_section() -> list[str]:
    tc = load("typechart")
    types = [r for r in read_csv("types") if int(r["id"]) < 10000 and r["identifier"] != "stellar"]
    if len(types) != 18:
        raise SystemExit(f"expected 18 types, got {len(types)}")
    name = {int(r["id"]): r["identifier"] for r in types}
    eff = {(int(r["damage_type_id"]), int(r["target_type_id"])): int(r["damage_factor"]) for r in read_csv("type_efficacy")}
    bad = []
    for atk, an in name.items():
        for dfn_id, dn in name.items():
            sd = SD_VALUE[tc[dn]["damageTaken"][an.capitalize()]]
            pa = eff.get((atk, dfn_id))
            if pa != sd:
                bad.append([an, dn, pa, sd])
    lines = ["## 属性相克（18×18）", ""]
    lines.append("PokeAPI `type_efficacy.damage_factor` 与 Showdown `TypeChart[防守方].damageTaken[攻击方]`"
                 "（0→100、1→200、2→50、3→0）全量比对，共 324 格。")
    lines.append("")
    if bad:
        lines.append(f"不一致：{len(bad)} 格。")
        lines.append("")
        lines += table(["攻击方", "防守方", "PokeAPI", "Showdown"], bad)
    else:
        lines.append("不一致：0 格。两边完全一致。")
    return lines


def learnset_section() -> list[str]:
    ls = load("learnsets")
    vgs = {r["identifier"]: int(r["id"]) for r in read_csv("version_groups")}
    hit = [g for g in SV_GROUPS if g in vgs]
    vg_ids = {vgs[g] for g in hit}
    meth = {int(r["id"]): METHODS[r["identifier"]] for r in read_csv("pokemon_move_methods") if r["identifier"] in METHODS}
    move_key = {int(r["id"]): sd_id(r["identifier"]) for r in read_csv("moves")}

    pa_moves: dict[int, set] = defaultdict(set)
    pa_levels: dict[int, set] = defaultdict(set)
    for r in read_csv("pokemon_moves"):
        if int(r["version_group_id"]) not in vg_ids:
            continue
        m = meth.get(int(r["pokemon_move_method_id"]))
        if m is None:
            continue
        pid, mk = int(r["pokemon_id"]), move_key[int(r["move_id"])]
        pa_moves[pid].add(mk)
        if m == "L":
            pa_levels[pid].add((mk, int(r["level"] or 0)))

    def sd_sets(key: str) -> tuple[set, set]:
        moves, levels = set(), set()
        for mk, srcs in ls.get(key, {}).get("learnset", {}).items():
            for s in srcs:
                if len(s) >= 2 and s[0] == "9" and s[1] in "LMTE":
                    moves.add(mk)
                    if s[1] == "L":
                        levels.add((mk, int(s[2:])))
        return moves, levels

    pokemon = sorted(read_csv("pokemon"), key=lambda r: int(r["id"]))
    eligible = {"1": [], "0": []}
    excluded_forms = 0
    for r in pokemon:
        pid = int(r["id"])
        if pid not in pa_moves:
            continue
        key = sd_id(r["identifier"])
        if sd_sets(key)[0]:
            eligible[r["is_default"]].append((r, key))
        elif r["is_default"] == "0":
            excluded_forms += 1

    forms = pick(eligible["0"], N_FORMS)
    defaults = pick(eligible["1"], N_DEFAULT + max(0, N_FORMS - len(forms)))
    sample = defaults + forms

    rows = []
    tot_inter = tot_union = tot_lvl_bad = tot_lvl = 0
    for r, key in sample:
        pid = int(r["id"])
        sm, sl = sd_sets(key)
        pm, pl = pa_moves[pid], pa_levels[pid]
        inter = pm & sm
        lvl_bad = len(pl ^ sl)
        tot_inter += len(inter)
        tot_union += len(pm | sm)
        tot_lvl_bad += lvl_bad
        tot_lvl += len(pl | sl)
        only_pa = ", ".join(sorted(pm - sm)) or "—"
        only_sd = ", ".join(sorted(sm - pm)) or "—"
        rows.append([pid, r["identifier"], key, len(pm), len(sm), len(inter), only_pa, only_sd, lvl_bad])

    lines = ["## 第 9 世代学习面抽样", ""]
    lines.append(f"PokeAPI 版本组：命中 {', '.join(hit)}（查找 {', '.join(SV_GROUPS)}）；学习方式限 level-up / machine / tutor / egg。")
    lines.append("Showdown 侧取来源字符串以 `9` 开头且第二位为 L/M/T/E 的条目。招式与宝可梦键均为 identifier 去掉非字母数字字符。")
    lines.append("")
    lines.append(f"- 合格默认形态：{len(eligible['1'])}；合格非默认形态：{len(eligible['0'])}；"
                 f"PokeAPI 有第 9 世代学习面但 Showdown 键不匹配而排除的非默认形态：{excluded_forms}")
    lines.append(f"- 抽样：默认形态 {len(defaults)}，非默认形态 {len(forms)}，共 {len(sample)}（按 id 排序均匀抽取）")
    lines.append("")
    lines += table(["id", "identifier", "showdown key", "PokeAPI 招式数", "Showdown 招式数", "交集",
                    "仅 PokeAPI", "仅 Showdown", "升级 (move, level) 不一致数"], rows)
    lines.append("")
    rate = tot_inter / tot_union * 100 if tot_union else 0
    lrate = (1 - tot_lvl_bad / tot_lvl) * 100 if tot_lvl else 0
    lines.append(f"整体招式集合一致率（交集/并集）：{tot_inter}/{tot_union} = {rate:.1f}%")
    lines.append(f"升级 (move, level) 对称差：{tot_lvl_bad}，并集 {tot_lvl}，一致率 {lrate:.1f}%")
    return lines, len(sample), len(forms)


def main() -> int:
    tc_lines = typechart_section()
    ls_lines, n, nf = learnset_section()
    lines = ["Showdown 数据由 `tools/phase0/showdown_dump.mjs` 经 Node 24 原生类型剥离导入后转为 JSON。", "",
             *tc_lines, "", *ls_lines]
    print(write_report("showdown-diff", "D0-6 Showdown 对比", ("pokeapi", "showdown"), lines))
    if n != N_DEFAULT + N_FORMS or nf < 1:
        print(f"sample size {n}, forms {nf}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())

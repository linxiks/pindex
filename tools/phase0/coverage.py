#!/usr/bin/env python3
"""D0-4: per-entity, per-language name and flavor-text coverage from PokeAPI CSV."""

from __future__ import annotations

import sys
from collections import defaultdict

from common import read_csv, lang_ids, table, write_report

COLS = ("zh-hans", "zh-hant", "en", "ja")


def covered(rows: list[dict], id_col: str, lang_col: str, text_col: str, langs: dict[str, int]) -> dict[str, set[int]]:
    """lang identifier -> set of entity ids with a non-empty text."""
    rev = {v: k for k, v in langs.items()}
    out: dict[str, set[int]] = defaultdict(set)
    for r in rows:
        lang = rev.get(int(r[lang_col]))
        if lang and r[text_col].strip():
            out[lang].add(int(r[id_col]))
    return out


def cov_row(label: str, ids: set[int], cov: dict[str, set[int]]) -> list:
    n = len(ids)
    return [label, n] + [f"{len(cov[l] & ids)}/{n}" for l in COLS]


def main() -> int:
    langs = lang_ids()
    lines: list[str] = []
    rows: list[list] = []

    species = {int(r["id"]): r["identifier"] for r in read_csv("pokemon_species")}
    sp_names = read_csv("pokemon_species_names")
    sp_cov = covered(sp_names, "pokemon_species_id", "local_language_id", "name", langs)
    rows.append(cov_row("species 名称", set(species), sp_cov))
    rows.append(cov_row("species genus", set(species), covered(sp_names, "pokemon_species_id", "local_language_id", "genus", langs)))

    moves = read_csv("moves")
    move_ids = {int(r["id"]) for r in moves}
    move_cov = covered(read_csv("move_names"), "move_id", "local_language_id", "name", langs)
    rows.append(cov_row("move 名称（id < 10000）", {i for i in move_ids if i < 10000}, move_cov))
    rows.append(cov_row("move 名称（全部）", move_ids, move_cov))

    abilities = read_csv("abilities")
    ab_ids = {int(r["id"]) for r in abilities}
    ab_cov = covered(read_csv("ability_names"), "ability_id", "local_language_id", "name", langs)
    rows.append(cov_row("ability 名称（is_main_series=1）", {int(r["id"]) for r in abilities if r["is_main_series"] == "1"}, ab_cov))
    rows.append(cov_row("ability 名称（全部）", ab_ids, ab_cov))

    simple = [
        ("item", "items", "item_names", "item_id"),
        ("type", "types", "type_names", "type_id"),
        ("nature", "natures", "nature_names", "nature_id"),
        ("stat", "stats", "stat_names", "stat_id"),
        ("version", "versions", "version_names", "version_id"),
        ("generation", "generations", "generation_names", "generation_id"),
        ("egg_group", "egg_groups", "egg_group_prose", "egg_group_id"),
    ]
    simple_cov: dict[str, tuple[dict[int, str], dict[str, set[int]]]] = {}
    for label, base, names, id_col in simple:
        ents = {int(r["id"]): r["identifier"] for r in read_csv(base)}
        cov = covered(read_csv(names), id_col, "local_language_id", "name", langs)
        simple_cov[label] = (ents, cov)
        rows.append(cov_row(f"{label} 名称", set(ents), cov))

    forms = {int(r["id"]): r["identifier"] for r in read_csv("pokemon_forms") if r["form_identifier"].strip()}
    form_cov = covered(read_csv("pokemon_form_names"), "pokemon_form_id", "local_language_id", "form_name", langs)
    rows.append(cov_row("pokemon_form 形态名（form_identifier 非空）", set(forms), form_cov))

    lines += ["## 名称覆盖", "", "分母为实体总数；名称 `strip()` 后非空才算覆盖。", ""]
    lines += table(["实体", "总数", *COLS], rows)
    lines.append("")

    # ---- flavor text ----
    rev = {v: k for k, v in langs.items()}
    versions = sorted(read_csv("versions"), key=lambda r: int(r["id"]))
    vgroups = sorted(read_csv("version_groups"), key=lambda r: int(r["order"]))

    sp_ft: dict[int, dict[str, set[int]]] = defaultdict(lambda: defaultdict(set))  # version -> lang -> species
    sp_versions: dict[int, set[int]] = defaultdict(set)  # species -> versions with any text
    for r in read_csv("pokemon_species_flavor_text"):
        if not r["flavor_text"].strip():
            continue
        sid, vid = int(r["species_id"]), int(r["version_id"])
        sp_versions[sid].add(vid)
        lang = rev.get(int(r["language_id"]))
        if lang:
            sp_ft[vid][lang].add(sid)

    lines += ["## 图鉴说明（pokemon_species_flavor_text，按版本）", ""]
    vrows, no_zh = [], []
    for v in versions:
        vid = int(v["id"])
        vrows.append([vid, v["identifier"]] + [len(sp_ft[vid][l]) for l in COLS])
        if not sp_ft[vid]["zh-hans"]:
            no_zh.append(v["identifier"])
    lines += table(["version_id", "version", *COLS], vrows)
    any_zh = set().union(*(sp_ft[v]["zh-hans"] for v in sp_ft))
    any_zh_hant = set().union(*(sp_ft[v]["zh-hant"] for v in sp_ft))
    latest_zh = sum(1 for sid, vs in sp_versions.items() if sid in sp_ft[max(vs)]["zh-hans"])
    no_text = sorted(set(species) - set(sp_versions))
    lines += [
        "",
        f"- 至少有一条 zh-Hans 说明的物种：{len(any_zh & set(species))}/{len(species)}",
        f"- 至少有一条 zh-Hant 说明的物种：{len(any_zh_hant & set(species))}/{len(species)}",
        f"- 至少有一条 zh-Hans 或 zh-Hant 说明的物种：{len((any_zh | any_zh_hant) & set(species))}/{len(species)}",
        f"- 有任意语言说明的物种：{len(sp_versions)}/{len(species)}；完全无说明：{len(no_text)}"
        + (f"（id {no_text[0]}…{no_text[-1]}）" if no_text else ""),
        f"- \"最新有文本的版本\"含 zh-Hans 的物种：{latest_zh}/{len(sp_versions)}",
        f"- 没有任何 zh-Hans 说明的版本（{len(no_zh)} 个）：{', '.join(no_zh)}",
        "",
    ]

    for label, table_name, id_col, ents in (
        ("招式说明（move_flavor_text）", "move_flavor_text", "move_id", move_ids),
        ("特性说明（ability_flavor_text）", "ability_flavor_text", "ability_id", ab_ids),
    ):
        per: dict[int, dict[str, set[int]]] = defaultdict(lambda: defaultdict(set))
        for r in read_csv(table_name):
            lang = rev.get(int(r["language_id"]))
            if lang and r["flavor_text"].strip():
                per[int(r["version_group_id"])][lang].add(int(r[id_col]))
        lines += [f"## {label}，按版本组", ""]
        grows = [[int(g["id"]), g["identifier"]] + [len(per[int(g["id"])][l]) for l in COLS] for g in vgroups]
        lines += table(["version_group_id", "version_group", *COLS], grows)
        union = {l: set().union(*(per[g][l] for g in per)) for l in COLS}
        lines += ["", "至少有一条说明的实体数（任一版本组）：" + "，".join(f"{l} {len(union[l] & ents)}/{len(ents)}" for l in COLS), ""]

    # ---- missing zh-Hans names ----
    lines += ["## 缺 zh-Hans 名称的实体", ""]
    move_ident = {int(r["id"]): r["identifier"] for r in moves}
    ab_ident = {int(r["id"]): r["identifier"] for r in abilities}
    items_ident = simple_cov["item"][0]
    for label, idents, cov in (
        ("move", move_ident, move_cov),
        ("ability", ab_ident, ab_cov),
        ("item", items_ident, simple_cov["item"][1]),
    ):
        missing = sorted(set(idents) - cov["zh-hans"])
        lines.append(f"### {label}（{len(missing)}）")
        lines.append("")
        lines.append(", ".join(f"{i} {idents[i]}" for i in missing) if missing else "无")
        lines.append("")

    path = write_report("coverage", "D0-4 PokeAPI 多语言覆盖率", ("pokeapi",), lines)
    print(path)

    n_zh = len(sp_cov["zh-hans"] & set(species))
    if n_zh != len(species):
        print(f"species zh-Hans incomplete: {n_zh}/{len(species)}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())

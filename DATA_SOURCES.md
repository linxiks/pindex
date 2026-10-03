# 数据来源

本文件记录 pindex 使用或评估过的每个数据来源：固定版本、获取日期、许可证、用途和修改方式。

代码许可证与数据许可证分开判断。仓库代码按 MIT 发布（见 `LICENSE`），该许可证不覆盖任何第三方数据或图片。第三方署名与商标声明见 `NOTICE`。

## 来源一览

获取日期为 2026-10-03。"状态"含义：

- 采用：进入构建流程，内容出现在 `pokedex.db` 中。
- 校验源：只用于比对，不写入 `pokedex.db`。
- 仅参照：只参考字段结构，不进入构建。
- 暂缓：许可证未明，不进入构建。
- 不使用：不读取、不复制。
- 待评估：需要先完成评估任务再决定。

| 来源 | 用途 | 许可证 | 固定版本 | 获取日期 | 状态 |
|---|---|---|---|---|---|
| [PokeAPI/pokeapi](https://github.com/PokeAPI/pokeapi) `data/v2/csv/` | 结构化关系数据与中文文本的主来源 | BSD-3-Clause（代码与数据） | `bc92d3b6029ef1abe9e7ad424c400b338f3c11fe`（2026-09-30 提交） | 2026-10-03 | 采用 |
| [PokeAPI/api-data](https://github.com/PokeAPI/api-data) | 字段参照 | BSD-3-Clause | 不固定 | 2026-10-03 | 仅参照 |
| [smogon/pokemon-showdown](https://github.com/smogon/pokemon-showdown) `data/*.ts` | 学习面与属性克制表交叉校验 | MIT | `3661ce40bf9001d185ce078b8920e12304204609`（2026-10-02 提交） | 2026-10-03 | 校验源 |
| [sindresorhus/pokemon](https://github.com/sindresorhus/pokemon) `data/zh-hans.json`、`data/zh-hant.json` | 物种中文名交叉校验 | MIT | `671b777e5df2b4795050adec3e4c8a9a52cafafb`（2026-07-13 提交） | 2026-10-03 | 校验源 |
| [42arch/pokemon-dataset-zh](https://github.com/42arch/pokemon-dataset-zh) | 中文补缺候选（已否决） | 代码 MIT；数据抓取自 52poke（CC BY-NC-SA 3.0） | `82ce04e611d19a12556c3955125b048b36187f52`（评估时，2026-03-17 提交） | 2026-10-03 | 不使用 |
| [wenitrys/pokemon-atlas-cn](https://github.com/wenitrys/pokemon-atlas-cn) | 无 | 仓库无 LICENSE | 不适用 | 2026-10-03 | 不使用 |
| [PokeAPI/sprites](https://github.com/PokeAPI/sprites) | 图片 | 仓库 CC0 1.0；图像版权归 The Pokémon Company | `bfb75391935310368065096fa08c51e8970bc43e`（2026-10-01 提交，D0-8 体积测量用） | 2026-10-03 | 待评估 |
| play.pokemonshowdown.com `data/*.json` | 无 | 归属不明（客户端仓库为 AGPL-3.0） | 不适用 | 2026-10-03 | 不使用 |

说明：

- 各来源固定在上表的 commit，机器可读副本（含每个文件的 sha256、字节数和 CSV 表头）在 `data/metadata/sources.json`。重新获取数据时必须同时改写两处的 commit 和获取日期。
- PokeAPI/sprites 的 `LICENCE.txt` 写明 "All image contents within are Copyright The Pokémon Company. This repository is distributed under CC0 1.0 Universal."。CC0 不能被理解为已经取得图像使用授权。D0-8 已实测体积（`data/reports/sprites-size.md`：96px 默认图 2.11 MB，official-artwork 192.35 MB，HOME 181.87 MB），图片策略由 `docs/development-plan.md` 的 D4-7 决定，状态保持"待评估"。
- 42arch 不可用（阶段 0 任务 D0-9 结论）。依据：52poke 神奇宝贝百科的内容按 CC BY-NC-SA 3.0 授权（`https://wiki.52poke.com/api.php?action=query&meta=siteinfo&siprop=rightsinfo&format=json` 返回"署名-非商业性使用-相同方式共享 3.0"，版权声明页为 `https://wiki.52poke.com/wiki/神奇宝贝百科:版权声明`）；42arch 的 MIT 只覆盖其代码，不能对抓取的数据重新授权；NC 条款禁止商业使用；SA 条款会要求合并后的 `pokedex.db` 整体按 BY-NC-SA 发布，与 PokeAPI 的 BSD-3-Clause 和本项目的发布方式冲突。仓库内置的图片也没有许可声明。该判断基于许可证文本，不是法律意见；取得 52poke 书面授权后可重新评估。
- wenitrys/pokemon-atlas-cn 没有 LICENSE，保留所有权利。本项目不复制其任何文件，只参考其公开的设计思路（回退顺序与回退原因记录）。

## 修改方式

构建流程为 raw → normalized → SQLite，由 `tools/data-builder/` 中的 Python 脚本完成。获取一步已实现，标准化与合并仍在规划中（阶段 1）。

1. 获取：`python3 tools/data-builder/fetch.py`（仓库根目录运行）按 `data/metadata/sources.json` 中的 repo、commit 和文件列表，从 `raw.githubusercontent.com` 下载到 `data/raw/<来源>/`，并记录 sha256、字节数和 CSV 表头。已记录 sha256 的文件会被校验，不一致时以退出码 1 结束。原始文件不做任何修改，也不入库（见 `.gitignore`）。
2. 标准化：把各来源的 CSV / JSON 转成统一结构，写入 `data/normalized/`。字段重命名、类型转换（如字符串数字转整数）、空值统一为 NULL。
3. 合并：按 `docs/development-plan.md` 中的字段权威来源表选取数据。中文文本回退顺序为 zh-Hans → zh-Hant → en，不自动生成或翻译任何中文文本。
4. 冲突处理：不同来源的同一字段不一致时，不静默覆盖，写入冲突日志并由构建校验报告。
5. 来源标记：每条文本记录带 `source` 字段，标明具体来自哪个来源和语言。
6. 输出：生成只读的 `data/generated/pokedex.db`，其中 `meta` 表记录 `schema_version`、`data_version`、`build_date` 和 `source_versions`。

属性克制倍率不预先存储，由应用根据 `type_efficacy` 动态计算。

## 更新流程

1. 在上表中更新目标来源的固定版本和获取日期。
2. 重新运行获取脚本，确认 `data/metadata/sources.json` 中的 commit 与本文件一致。
3. 重新运行构建脚本，确认完整性检查全部通过，冲突日志已人工复核。
4. 递增 `data_version`，重新生成 `pokedex.db`。
5. 用户数据存放在独立的 `user.db`，替换 `pokedex.db` 不会影响收藏与历史记录。

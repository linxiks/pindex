# D0-6 Showdown 对比

数据来源：PokeAPI/pokeapi@bc92d3b6029e，smogon/pokemon-showdown@3661ce40bf90。由 `tools/phase0/` 脚本生成，勿手改。

Showdown 数据由 `tools/phase0/showdown_dump.mjs` 经 Node 24 原生类型剥离导入后转为 JSON。

## 属性相克（18×18）

PokeAPI `type_efficacy.damage_factor` 与 Showdown `TypeChart[防守方].damageTaken[攻击方]`（0→100、1→200、2→50、3→0）全量比对，共 324 格。

不一致：0 格。两边完全一致。

## 第 9 世代学习面抽样

PokeAPI 版本组：命中 scarlet-violet, the-teal-mask, the-indigo-disk（查找 scarlet-violet, the-teal-mask, the-indigo-disk）；学习方式限 level-up / machine / tutor / egg。
Showdown 侧取来源字符串以 `9` 开头且第二位为 L/M/T/E 的条目。招式与宝可梦键均为 identifier 去掉非字母数字字符。

- 合格默认形态：705；合格非默认形态：53；PokeAPI 有第 9 世代学习面但 Showdown 键不匹配而排除的非默认形态：81
- 抽样：默认形态 20，非默认形态 10，共 30（按 id 排序均匀抽取）

| id | identifier | showdown key | PokeAPI 招式数 | Showdown 招式数 | 交集 | 仅 PokeAPI | 仅 Showdown | 升级 (move, level) 不一致数 |
|---|---|---|---|---|---|---|---|---|
| 1 | bulbasaur | bulbasaur | 48 | 48 | 48 | — | — | 0 |
| 61 | poliwhirl | poliwhirl | 52 | 50 | 50 | mist, splash | — | 0 |
| 113 | chansey | chansey | 77 | 74 | 74 | healbell, present, seismictoss | — | 0 |
| 164 | noctowl | noctowl | 64 | 62 | 61 | supersonic, whirlwind, wingattack | defog | 1 |
| 211 | qwilfish | qwilfish | 70 | 70 | 70 | — | — | 0 |
| 256 | combusken | combusken | 66 | 60 | 60 | counter, crushclaw, feint, lastresort, nightslash, peck | — | 2 |
| 317 | swalot | swalot | 59 | 59 | 59 | — | — | 0 |
| 378 | regice | regice | 47 | 47 | 47 | — | — | 0 |
| 425 | drifloon | drifloon | 60 | 60 | 60 | — | — | 0 |
| 475 | gallade | gallade | 107 | 108 | 107 | — | metronome | 0 |
| 548 | petilil | petilil | 35 | 35 | 35 | — | — | 0 |
| 615 | cryogonal | cryogonal | 47 | 47 | 47 | — | — | 0 |
| 667 | litleo | litleo | 49 | 49 | 49 | — | — | 0 |
| 729 | brionne | brionne | 45 | 42 | 42 | aquaring, lifedew, perishsong | — | 0 |
| 784 | kommo-o | kommoo | 78 | 76 | 76 | counter, dragonbreath | — | 1 |
| 856 | hatenna | hatenna | 46 | 46 | 46 | — | — | 0 |
| 908 | meowscarada | meowscarada | 72 | 72 | 72 | — | — | 2 |
| 948 | toedscool | toedscool | 58 | 58 | 58 | — | — | 0 |
| 988 | slither-wing | slitherwing | 57 | 57 | 57 | — | — | 1 |
| 1025 | pecharunt | pecharunt | 35 | 35 | 35 | — | — | 3 |
| 10022 | kyurem-black | kyuremblack | 63 | 63 | 63 | — | — | 0 |
| 10104 | ninetales-alola | ninetalesalola | 55 | 58 | 50 | babydolleyes, flail, howl, hypnosis, moonblast | aurorabeam, auroraveil, disable, extrasensory, freezedry, iceshard, mist, powdersnow | 12 |
| 10110 | graveler-alola | graveleralola | 65 | 59 | 59 | block, counter, flail, screech, wideguard, zapcannon | — | 0 |
| 10126 | lycanroc-midnight | lycanrocmidnight | 65 | 65 | 65 | — | — | 0 |
| 10167 | weezing-galar | weezinggalar | 60 | 60 | 60 | — | — | 0 |
| 10191 | urshifu-rapid-strike | urshifurapidstrike | 60 | 60 | 60 | — | — | 0 |
| 10231 | voltorb-hisui | voltorbhisui | 47 | 47 | 47 | — | — | 0 |
| 10236 | samurott-hisui | samurotthisui | 65 | 65 | 65 | — | — | 0 |
| 10242 | goodra-hisui | goodrahisui | 66 | 66 | 66 | — | — | 0 |
| 10272 | ursaluna-bloodmoon | ursalunabloodmoon | 77 | 69 | 69 | aerialace, bulkup, charm, closecombat, drainpunch, faketears, metronome, playrough | — | 1 |

整体招式集合一致率（交集/并集）：1757/1805 = 97.3%
升级 (move, level) 对称差：23，并集 516，一致率 95.5%

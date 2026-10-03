# D0-8 图片体积（PokeAPI/sprites）

数据来源：PokeAPI/sprites@bfb753919353。由 `tools/phase0/` 脚本生成，勿手改。

通过 GitHub git trees API（非递归）读取固定 commit 下各目录的直接 `.png` blob 大小；不含子目录。

| 目录 | 用途 | PNG 文件数 | 总字节 | MB | 编号 1–1025（数/MB） | 编号 ≥10001（数/MB） | 其他（数/MB） |
|---|---|---|---|---|---|---|---|
| sprites/pokemon | 96px 默认 | 1542 | 2217633 | 2.11 | 1025 / 1.04 | 317 / 0.39 | 200 / 0.69 |
| sprites/pokemon/shiny | 96px 闪光 | 1542 | 1877515 | 1.79 | 1025 / 1.00 | 317 / 0.40 | 200 / 0.39 |
| sprites/pokemon/other/official-artwork | 官方立绘 | 1538 | 201695262 | 192.35 | 1025 / 126.52 | 322 / 40.80 | 191 / 25.04 |
| sprites/pokemon/other/official-artwork/shiny | 官方立绘 闪光 | 1531 | 211083499 | 201.30 | 1025 / 132.00 | 315 / 44.51 | 191 / 24.80 |
| sprites/pokemon/other/home | HOME | 1531 | 190709521 | 181.87 | 1025 / 120.41 | 310 / 39.34 | 196 / 22.13 |
| sprites/pokemon/other/home/shiny | HOME 闪光 | 1523 | 192180145 | 183.28 | 1025 / 121.55 | 303 / 39.00 | 195 / 22.73 |

六个目录合计：9207 个文件，799763575 字节（762.71 MB）。

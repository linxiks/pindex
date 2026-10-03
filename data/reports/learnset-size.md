# D0-7 学习面体积

数据来源：PokeAPI/pokeapi@bc92d3b6029e。由 `tools/phase0/` 脚本生成，勿手改。

`pokemon_moves.csv` 总行数：638321；涉及宝可梦：1303。

五列键 (pokemon_id, version_group_id, move_id, pokemon_move_method_id, level；level 空值按 0) 重复组数：0。

## 体积（SQLite WITHOUT ROWID，主键为五列键，INSERT OR IGNORE 后 VACUUM）

- 方案 A：全部版本组
- 方案 B：每个宝可梦只保留其有数据的版本组中 `version_groups.order` 最大的一个

| 方案 | CSV 行数 | 入库行数 | 无索引 字节 | 无索引 MB | 加 (move_id, version_group_id) 索引 字节 | 加索引 MB |
|---|---|---|---|---|---|---|
| A | 638321 | 638321 | 9928704 | 9.47 | 19181568 | 18.29 |
| B | 75243 | 75243 | 1196032 | 1.14 | 2310144 | 2.20 |

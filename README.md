# pindex

中文离线宝可梦资料查询工具。安卓应用，简体中文优先，核心图鉴数据完全离线，覆盖第 1～9 世代。

## 当前状态

阶段 1：数据构建器已完成，可生成离线图鉴库 `pokedex.db`。尚无安卓应用代码。

## 文档

- [开发需求](docs/project.md)
- [UI/UX 设计](docs/design.md)
- [开发计划](docs/development-plan.md)
- [数据来源](DATA_SOURCES.md)
- [第三方署名与商标声明](NOTICE)

## 目录结构

```text
app/                    安卓应用（规划中）
tools/data-builder/     Python 数据构建器（仅标准库）
tools/phase0/           阶段 0 覆盖率与对比脚本
data/
  raw/                  原始数据（不入库，由 fetch.py 下载）
  normalized/           阶段 0 Showdown 转储（不入库）
  generated/            生成的 pokedex.db、conflicts.csv、build-report.md（不入库）
  metadata/             sources.json（固定 commit 与 sha256）
  reports/              阶段 0 报告
docs/                   需求、设计与开发计划
```

## 构建

在仓库根目录运行：

```sh
python3 tools/data-builder/fetch.py   # 下载固定版本的原始数据
python3 tools/data-builder/build.py   # 生成 data/generated/pokedex.db
```

单元测试：`python3 -m unittest discover -s tools/data-builder/tests -v`。

## 许可证

代码按 [MIT](LICENSE) 发布。该许可证不覆盖第三方数据与图片，数据许可见 [DATA_SOURCES.md](DATA_SOURCES.md)。

Pokémon and Pokémon character names are trademarks of Nintendo, Creatures Inc. and GAME FREAK inc. 本项目为非官方项目，与上述公司无关联，也未获其认可。

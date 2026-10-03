# pindex

中文离线宝可梦资料查询工具。安卓应用，简体中文优先，核心图鉴数据完全离线，覆盖第 1～9 世代。

## 当前状态

阶段 0：数据可行性验证。尚无可运行代码。

## 文档

- [开发需求](docs/project.md)
- [UI/UX 设计](docs/design.md)
- [开发计划](docs/development-plan.md)
- [数据来源](DATA_SOURCES.md)
- [第三方署名与商标声明](NOTICE)

## 规划中的目录结构

```text
app/                    安卓应用（规划中）
tools/data-builder/     Python 数据构建器（规划中）
data/
  raw/                  原始数据（规划中）
  normalized/           标准化数据（规划中）
  generated/            生成的 pokedex.db（规划中）
  metadata/             sources.json（规划中）
docs/                   需求、设计与开发计划
```

## 许可证

代码按 [MIT](LICENSE) 发布。该许可证不覆盖第三方数据与图片，数据许可见 [DATA_SOURCES.md](DATA_SOURCES.md)。

Pokémon and Pokémon character names are trademarks of Nintendo, Creatures Inc. and GAME FREAK inc. 本项目为非官方项目，与上述公司无关联，也未获其认可。

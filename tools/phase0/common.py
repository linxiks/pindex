"""Shared helpers for phase-0 feasibility scripts (run from the repo root)."""

from __future__ import annotations

import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RAW = ROOT / "data" / "raw"
NORMALIZED = ROOT / "data" / "normalized"
REPORTS = ROOT / "data" / "reports"
SOURCES = ROOT / "data" / "metadata" / "sources.json"

LANGS = ("zh-hans", "zh-hant", "en", "ja", "ja-hrkt")


def read_csv(name: str) -> list[dict]:
    with open(RAW / "pokeapi" / f"{name}.csv", encoding="utf-8", newline="") as f:
        return list(csv.DictReader(f))


def lang_ids() -> dict[str, int]:
    by_ident = {r["identifier"]: int(r["id"]) for r in read_csv("languages")}
    out = {}
    for ident in LANGS:
        if ident not in by_ident:
            raise SystemExit(f"language missing: {ident}")
        out[ident] = by_ident[ident]
    return out


def source_line(*keys: str) -> str:
    srcs = json.loads(SOURCES.read_text(encoding="utf-8"))["sources"]
    parts = [f"{srcs[k]['repo']}@{srcs[k]['commit'][:12]}" for k in keys]
    return "数据来源：" + "，".join(parts)


def write_report(name: str, title: str, sources: tuple[str, ...], lines: list[str]) -> Path:
    REPORTS.mkdir(parents=True, exist_ok=True)
    path = REPORTS / f"{name}.md"
    body = [f"# {title}", "", source_line(*sources) + "。由 `tools/phase0/` 脚本生成，勿手改。", "", *lines]
    path.write_text("\n".join(body).rstrip() + "\n", encoding="utf-8")
    return path


def table(header: list[str], rows: list[list]) -> list[str]:
    out = ["| " + " | ".join(header) + " |", "|" + "---|" * len(header)]
    for r in rows:
        out.append("| " + " | ".join(str(c).replace("|", "\\|").replace("\n", " ") for c in r) + " |")
    return out

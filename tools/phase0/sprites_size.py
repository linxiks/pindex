#!/usr/bin/env python3
"""D0-8: measure PokeAPI/sprites PNG counts and bytes at the pinned commit.

Uses the GitHub git trees API (non-recursive, ~10 requests). A blobless clone is not
used: `git ls-tree -l` on a partial clone must read blob sizes and lazily fetches blobs.
Exit 1 on truncated tree; exit 2 when rate limited (rerun after the printed time).
Output: data/reports/sprites-size.md
"""

from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from common import SOURCES, table, write_report  # noqa: E402

API = "https://api.github.com/repos/PokeAPI/sprites"
DIRS = [
    ("sprites/pokemon", "96px 默认"),
    ("sprites/pokemon/shiny", "96px 闪光"),
    ("sprites/pokemon/other/official-artwork", "官方立绘"),
    ("sprites/pokemon/other/official-artwork/shiny", "官方立绘 闪光"),
    ("sprites/pokemon/other/home", "HOME"),
    ("sprites/pokemon/other/home/shiny", "HOME 闪光"),
]


class RateLimited(Exception):
    pass


def get(url: str) -> dict:
    headers = {"Accept": "application/vnd.github+json", "User-Agent": "pindex-phase0"}
    token = os.environ.get("GITHUB_TOKEN")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    try:
        with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=120) as r:
            return json.load(r)
    except urllib.error.HTTPError as e:
        if e.code in (403, 429) and e.headers.get("x-ratelimit-remaining") == "0":
            reset = int(e.headers.get("x-ratelimit-reset", "0"))
            raise RateLimited(datetime.fromtimestamp(reset, timezone.utc).isoformat()) from e
        raise


def tree(sha: str) -> list[dict]:
    t = get(f"{API}/git/trees/{sha}")
    if t.get("truncated"):
        raise SystemExit(f"tree truncated: {sha}")
    return t["tree"]


def main() -> int:
    commit = json.loads(SOURCES.read_text(encoding="utf-8"))["sources"]["pokeapi-sprites"]["commit"]
    cache: dict[str, list[dict] | None] = {}
    try:
        root = get(f"{API}/git/commits/{commit}")["tree"]["sha"]
        cache[""] = tree(root)

        def resolve(path: str) -> list[dict] | None:
            if path in cache:
                return cache[path]
            parent, _, name = path.rpartition("/")
            entries = resolve(parent)
            hit = next((e for e in entries or [] if e["path"] == name and e["type"] == "tree"), None)
            cache[path] = tree(hit["sha"]) if hit else None
            return cache[path]

        results = [(d, label, resolve(d)) for d, label in DIRS]
    except RateLimited as e:
        print(f"rate limited until {e}", file=sys.stderr)
        return 2

    rows = []
    total_files = total_bytes = 0
    for d, label, entries in results:
        if entries is None:
            rows.append([d, label, "不存在", "", "", "", "", ""])
            continue
        pngs = [e for e in entries if e["type"] == "blob" and e["path"].endswith(".png")]
        groups = {"main": [0, 0], "form": [0, 0], "other": [0, 0]}
        for e in pngs:
            stem = e["path"][:-4]
            g = "other"
            if stem.isdigit():
                n = int(stem)
                g = "main" if 1 <= n <= 1025 else "form" if n >= 10001 else "other"
            groups[g][0] += 1
            groups[g][1] += e["size"]
        size = sum(e["size"] for e in pngs)
        total_files += len(pngs)
        total_bytes += size
        fmt = lambda c: f"{c[0]} / {c[1] / 1024 / 1024:.2f}"  # noqa: E731
        rows.append([d, label, len(pngs), size, f"{size / 1024 / 1024:.2f}",
                     fmt(groups["main"]), fmt(groups["form"]), fmt(groups["other"])])

    lines = [
        "通过 GitHub git trees API（非递归）读取固定 commit 下各目录的直接 `.png` blob 大小；不含子目录。",
        "",
        *table(["目录", "用途", "PNG 文件数", "总字节", "MB",
                "编号 1–1025（数/MB）", "编号 ≥10001（数/MB）", "其他（数/MB）"], rows),
        "",
        f"六个目录合计：{total_files} 个文件，{total_bytes} 字节（{total_bytes / 1024 / 1024:.2f} MB）。",
    ]
    path = write_report("sprites-size", "D0-8 图片体积（PokeAPI/sprites）", ("pokeapi-sprites",), lines)
    print(path)
    return 0


if __name__ == "__main__":
    sys.exit(main())

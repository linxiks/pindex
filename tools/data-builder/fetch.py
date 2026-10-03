#!/usr/bin/env python3
"""Download pinned raw source files listed in data/metadata/sources.json.

For each source and file, the URL is
https://raw.githubusercontent.com/{repo}/{commit}/{path}; the local copy goes to
data/raw/{source_key}/{basename(path)}.

- File entry null: download, then record sha256, bytes and (for .csv) columns.
- File entry with sha256: skip if the local copy matches; otherwise download and
  verify. A mismatch prints "sha256 mismatch: <path>" and exits with code 1.

Bytes are written unchanged. sources.json is only rewritten when its content
changes, so a second run leaves it byte-identical.

Run from the repository root: python3 tools/data-builder/fetch.py
"""

from __future__ import annotations

import csv
import hashlib
import io
import json
import os
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCES = ROOT / "data" / "metadata" / "sources.json"
RAW = ROOT / "data" / "raw"
TIMEOUT = 60
RETRY_DELAYS = (2, 4, 8)
WORKERS = 6


class Mismatch(Exception):
    pass


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def download(url: str) -> bytes:
    last: Exception | None = None
    for attempt in range(len(RETRY_DELAYS) + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "pindex-fetch"})
            with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
                return resp.read()
        except (urllib.error.URLError, TimeoutError, ConnectionError, OSError) as e:
            last = e
            if attempt < len(RETRY_DELAYS):
                time.sleep(RETRY_DELAYS[attempt])
    raise RuntimeError(f"download failed: {url}: {last}")


def csv_columns(data: bytes) -> list[str]:
    text = data.decode("utf-8")
    return next(csv.reader(io.StringIO(text, newline="")), [])


def fetch_one(key: str, repo: str, commit: str, path: str, entry: dict | None) -> tuple[dict, bool]:
    """Return (entry, downloaded)."""
    local = RAW / key / Path(path).name
    if entry is not None and local.exists() and sha256_file(local) == entry["sha256"]:
        return entry, False

    url = f"https://raw.githubusercontent.com/{repo}/{commit}/{path}"
    data = download(url)
    digest = hashlib.sha256(data).hexdigest()
    local.parent.mkdir(parents=True, exist_ok=True)
    part = local.with_name(local.name + ".part")
    part.write_bytes(data)
    if entry is not None and digest != entry["sha256"]:
        part.unlink()
        raise Mismatch(str(local.relative_to(ROOT)))
    os.replace(part, local)

    new = {"sha256": digest, "bytes": len(data)}
    if path.endswith(".csv"):
        new["columns"] = csv_columns(data)
    return new, True


def main() -> int:
    original = SOURCES.read_text(encoding="utf-8")
    meta = json.loads(original)

    jobs = []
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        for key, src in meta["sources"].items():
            for path, entry in src["files"].items():
                fut = pool.submit(fetch_one, key, src["repo"], src["commit"], path, entry)
                jobs.append((key, path, fut))

        status = 0
        downloaded = 0
        for key, path, fut in jobs:
            try:
                entry, did = fut.result()
            except Mismatch as e:
                print(f"sha256 mismatch: {e}", file=sys.stderr)
                status = 1
                continue
            except RuntimeError as e:
                print(str(e), file=sys.stderr)
                status = status or 3
                continue
            meta["sources"][key]["files"][path] = entry
            downloaded += did

    # Persist recorded hashes even on partial failure so a rerun resumes.
    updated = json.dumps(meta, indent=2, ensure_ascii=False, sort_keys=True) + "\n"
    if updated != original:
        tmp = SOURCES.with_name(SOURCES.name + ".part")
        tmp.write_text(updated, encoding="utf-8")
        os.replace(tmp, SOURCES)

    total = len(jobs)
    print(f"files: {total}, downloaded: {downloaded}, skipped: {total - downloaded}")
    return status


if __name__ == "__main__":
    sys.exit(main())

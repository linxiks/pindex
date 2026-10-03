"""Language codes and the zh-Hans -> zh-Hant -> en display fallback."""

from __future__ import annotations

from collections.abc import Mapping

from .raw import Raw

# PokeAPI languages.identifier -> language code stored in pokedex.db
LANG_CODES = {"zh-hans": "zh-Hans", "zh-hant": "zh-Hant", "en": "en", "ja": "ja", "ja-hrkt": "ja-Hrkt"}

FALLBACK = ("zh-Hans", "zh-Hant", "en")


def lang_map(raw: Raw) -> dict[int, str]:
    """Return {PokeAPI language id: language code} for the five kept languages."""
    by_ident = {r["identifier"]: int(r["id"]) for r in raw.rows("languages")}
    out = {}
    for ident, code in LANG_CODES.items():
        if ident not in by_ident:
            raise ValueError(f"language missing: {ident}")
        out[by_ident[ident]] = code
    return out


def resolve(texts: Mapping[str, str]) -> tuple[str, str] | None:
    """Return (text, lang) for the first FALLBACK language present; ja / ja-Hrkt never qualify."""
    for lang in FALLBACK:
        text = texts.get(lang)
        if text:
            return text, lang
    return None

// D0-6: convert pinned Showdown TS data files to JSON via Node's built-in type stripping.
// Usage (repo root): node tools/phase0/showdown_dump.mjs
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..", "..");
const outDir = join(root, "data", "normalized", "showdown");
mkdirSync(outDir, { recursive: true });

for (const [file, name] of [["typechart", "TypeChart"], ["learnsets", "Learnsets"], ["pokedex", "Pokedex"]]) {
  const mod = await import(pathToFileURL(join(root, "data", "raw", "showdown", `${file}.ts`)).href);
  if (!mod[name]) throw new Error(`export ${name} missing in ${file}.ts`);
  writeFileSync(join(outDir, `${file}.json`), JSON.stringify(mod[name]));
  console.log(`${file}.json: ${Object.keys(mod[name]).length} keys`);
}

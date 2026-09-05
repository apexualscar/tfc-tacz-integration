# TFC TacZ Integration

A Forge mod that re-tiers every Timeless and Classics Zero (TACZ) crafting recipe
onto TerraFirmaCraft's iron progression.

TACZ recipes all want `forge:ingots/iron`. TFC has no vanilla iron ingot, so none
of them are craftable in TFC.

Iron in TFC comes in seven tiers, from wrought iron to blue/red steel. This mod
maps each gun, attachment, and iron-using ammo recipe onto that ladder: cheaper,
older weapons use early-game metal, modern high-pressure weapons need the
expensive stuff.

## How it works

TACZ ships its recipes in the bundled `tacz_default_gun` pack, which gets
re-copied every launch and lives inside the mod instance. Editing it breaks on
every TACZ update.

So this mod writes a separate gunpack, `zz_tacz_tfc_integration`, that shadows
the default recipes without touching them. The default pack keeps regenerating,
and the override wins.

Why the `zz_` prefix? TACZ merges every folder in `tacz/` into one aggregate
pack, and its recipe loader resolves duplicate recipe IDs last-wins in
directory enumeration order, alphabetical on NTFS. The prefix sorts this folder
after `tacz_default_gun`, so the override wins the tie.

The mod generates its gunpack on startup, so it always matches the installed
TACZ version. New guns added in a TACZ update get picked up automatically.

## Install

1. Install [TACZ](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero)
   and [TerraFirmaCraft](https://www.curseforge.com/minecraft/mc-mods/terrafirmacraft).
2. Drop this mod's `.jar` into your instance's `mods/` folder.
3. Launch. No config, no commands.

## Customizing the progression

Both config files live in `config/tacz-tfc-integration/` after first launch.
They're copied on first run, so you can delete one to reset it.

- `tier-metals.toml` maps each tier to a metal ingot item ID. Swap `t1` from
  wrought iron to copper if you want.
- `recipe-tiers.toml` maps each gun, attachment, and ammo to a tier. Move the
  AK-47 from `t3` to `t1` and it now costs wrought iron.

Both are hot-read on startup. Edit a file and restart (or re-enter the world).

## Build

Requires Java 17 and the two mod jars placed in `libs/` for dev only.

```
.\gradlew.bat build
```

Produces `build/libs/tacz-tfc-integration-<version>.jar`.

The source-of-truth tier map still lives in `data/tier-map.json` and
`scripts/generate-recipes.ps1`, used by the dev workflow. See
`docs/TIER-MAPPING.md` for the per-gun tier rationale.

## Compatibility

- Minecraft 1.20.1, Forge 47.x
- TACZ 1.x
- TerraFirmaCraft (provides `tfc:metal/ingot/*`)

## License

MIT. See [LICENSE](./LICENSE).

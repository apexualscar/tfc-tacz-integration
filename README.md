# TACZ × TerraFirmaCraft — Gun Recipe Progression

A standalone **TACZ gunpack override** that re-tiers every Timeless and Classics
Zero (TACZ) crafting recipe to use **TerraFirmaCraft's iron progression** instead
of the generic (and in TFC, unusable) `forge:ingots/iron`.

In a TerraFirmaCraft world there is no vanilla/forge iron ingot. Instead iron
comes in seven tiers, from wrought iron up to industrial iron. This pack maps
each gun, attachment, and iron-using ammunition recipe onto that ladder so
cheap/technologically older weapons use early-game metal and modern/high-pressure
weapons need the expensive stuff.

## Why an override gunpack?

TACZ ships its recipes in the bundled `tacz_default_gun` pack, which is
**re-copied every launch** and lives inside the mod instance. Editing it is
fragile and unshippable. This project instead ships a **separate gunpack** that
shadows the default recipes without touching them.

- Survives TACZ updates (the default pack keeps getting regenerated).
- Drops into any modpack: copy the folder into `minecraft/tacz/`.
- Fully portable and open-source.

> **Why `zz_`?** TACZ merges every folder in `tacz/` into a single aggregate pack
> and its recipe loader resolves duplicate recipe IDs **last-wins** in directory
> enumeration order (alphabetical on NTFS). The override folder is prefixed
> `zz_` so it enumerates *after* `tacz_default_gun` and wins the tie. Do not
> rename the folder to something that sorts before `tacz_default_gun`, or the
> default recipes will win and nothing will change.

## Install

1. Copy the `zz_tacz_tfc_progression` folder into your instance's
   `minecraft/tacz/` directory (next to `tacz_default_gun`).
2. Launch / re-enter the world. No config, no commands.

To distribute as a zip, run the build script (see below). Extract the zip
inside `minecraft/tacz/` — it contains the correctly-named
`zz_tacz_tfc_progression` folder.

## Build

```
.\build.ps1
```

Copies the recipe overrides into a fresh gunpack and produces a distributable
`dist/tacz_tfc_progression.zip`.

Requires a TerraFirmaCraft iron-tier metal reference. See `docs/TIER-MAPPING.md`
for the full per-gun tier assignments.

## Compatibility

- Minecraft 1.20.1, Forge 47.x
- TACZ (Timeless and Classics Zero) 1.x
- TerraFirmaCraft (provides `tfc:metal/ingot/*`)

## License

MIT — see [LICENSE](./LICENSE).

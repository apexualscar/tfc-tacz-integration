# TFC TacZ Integration

A standalone TACZ gunpack override that re-tiers every Timeless and Classics
Zero (TACZ) crafting recipe onto TerraFirmaCraft's iron progression, instead of
the generic `forge:ingots/iron`. TFC has no vanilla iron ingot, so those recipes
are unusable in a TFC world.

Iron in TFC comes in seven tiers, from wrought iron to industrial iron. This
pack maps each gun, attachment, and iron-using ammo recipe onto that ladder:
cheaper, older weapons use early-game metal; modern, high-pressure weapons need
the expensive stuff.

## Why an override gunpack

TACZ ships its recipes in the bundled `tacz_default_gun` pack, which is
re-copied every launch and lives inside the mod instance. Editing it breaks on
every TACZ update. So this project ships a separate gunpack that shadows the
default recipes without touching them. The default pack keeps getting
regenerated, and the override just wins.

Why is the folder named `zz_`? TACZ merges every folder in `tacz/` into one
aggregate pack, and its recipe loader resolves duplicate recipe IDs last-wins
in directory enumeration order (alphabetical on NTFS). The `zz_` prefix makes
this folder enumerate after `tacz_default_gun`, so the override wins the tie.
Rename the folder to something that sorts first and the default recipes win,
which changes nothing.

## Install

1. Copy the `zz_tacz_tfc_progression` folder into your instance's
   `minecraft/tacz/` directory, next to `tacz_default_gun`.
2. Launch or re-enter the world. No config, no commands.

To hand it out as a zip, run `build.ps1` (below) and extract it inside
`minecraft/tacz/`. The zip contains the correctly named `zz_tacz_tfc_progression`
folder.

## Build

```
.\build.ps1
```

Rebuilds the gunpack from the generator and produces `dist/tacz_tfc_progression.zip`.
See `docs/TIER-MAPPING.md` for the per-gun tier assignments.

## Compatibility

- Minecraft 1.20.1, Forge 47.x
- TACZ 1.x
- TerraFirmaCraft (provides `tfc:metal/ingot/*`)

## License

MIT. See [LICENSE](./LICENSE).

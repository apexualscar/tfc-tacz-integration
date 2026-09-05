# Tier Mapping

TACZ recipes are re-tiered onto **TerraFirmaCraft's iron progression**. Each
recipe's `forge:ingots/iron` (and `forge:nuggets/iron`) material is replaced
with the matching TFC metal for that tier.

## The scale

| Tier | Metal | Ingot item | In-game position |
|------|-------|-----------|------------------|
| t1 | Wrought Iron | `tfc:metal/ingot/wrought_iron` | First forgeable iron |
| t2 | Pig Iron | `tfc:metal/ingot/pig_iron` | Cheap cast iron |
| t3 | High Carbon Steel | `tfc:metal/ingot/high_carbon_steel` | First real steel |
| t4 | Steel | `tfc:metal/ingot/steel` | Refined steel |
| t5 | Black Steel | `tfc:metal/ingot/black_steel` | High-grade tool steel |
| t6 | Blue Steel | `tfc:metal/ingot/blue_steel` | Premium steel |
| t7 | Industrial Iron | `minecraft:netherite_ingot` | End-game (netherite replacement) |

## Guns

### t1 — Wrought Iron (early / simple / cheap)
`springfield1873`, `db_short`, `db_long`, `m1911`, `cz75`, `glock_17`, `m9a4`, `uzi`, `m870`

### t2 — Pig Iron (early blowback / classic action)
`b93r`, `hk_mp5a5`, `sks_tactical`, `m16a1`, `type_81`, `qbz_95`, `lonetrail`, `rhino357`

### t3 — High Carbon Steel (WWII–mid-century rifles)
`ak47`, `m4a1`, `m16a4`, `fn_fal`, `hk_g3`, `kar98`, `m700`, `p320`, `g36k`, `scar_l`, `rpk`, `rpg7`

### t4 — Steel (modern, gold/quartz-marked)
`hk416d`, `aug`, `qbz_191`, `mk14`, `scar_h`, `m249`, `m1014`, `spas_12`, `m320`, `vector45`, `ump45`, `p90`, `deagle`, `hk_mk23`, `taurus500`, `timeless50`

### t5 — Black Steel (heavy / precision / LMG)
`spr15hb`, `fn_evolys`, `aa12`, `deagle_golden`, `m107`, `m95`

### t6 — Blue Steel (top-pressure snipers / minigun)
`ai_awp`, `minigun`

> `m107` / `m95` / `minigun` already carry a separate `forge:ingots/netherite`
> line in their stock recipes; that line is left alone. Their main iron lines
> move to the tier shown above.

## Ammunition

Three ammo recipes use iron:
- `12g` → t1 (wrought iron nuggets) — shotgun shells; the base for all shotgun ammunition
- `40mm` → t2 (pig iron)
- `rpg_rocket` → t3 (high carbon steel)

The rest are copper-cased and use `forge:ingots/copper`, which already resolves
correctly in TFC, so they are left untouched.

## Attachments

Iron-using attachments are tiered by mechanical complexity and cost:

- **t1** — simple fixings: `bayonet_6h3`, `bayonet_m9`, `grip_rk6`, `oem_stock_light`, small reflex dots, `stock_m4ss`, `laser_nightstick`
- **t2** — grips / basic rails / pistols dots / basic stock: `grip_*`, `laser_compact`, `laser_lopro`, `oem_stock_heavy`, `oem_stock_tactical`, `scope_1873_6x`, `scope_retro_2x`, `sight_coyote`, `sight_okp7`, `sight_srs_02`, `stock_moe`, `stock_ripstock`, `stock_sba3`, `stock_carbon_bone_c5`, low-tier mags, `muzzle_brake_cyclone_d2`, `muzzle_compensator_trident`, `muzzle_silencer_mirage`, `muzzle_silencer_wraith`
- **t3** — locks / mid scopes: `scope_98k`, `scope_contender`, `muzzle_brake_pioneer`, `muzzle_brake_timeless50`, `stock_ak12`, `stock_hk_slim_line`, `stock_militech_b5`, `muzzle_silencer_ptilopsis`, mid-tier mags, `stock_tactical_spas_12`
- **t4** — modern optics / advanced muzzles: `scope_acog_ta31`, `scope_elcan_4x`, `scope_hamr`, `scope_lpvo_1_6`, `scope_qmk152`, `scope_standard_8x`, `scope_vudu`, `muzzle_brake_cthulhu`, `muzzle_brake_mastiff_sg`, `muzzle_brake_trex`, `muzzle_choke_sg`, `muzzle_silencer_knight_qd`, `muzzle_silencer_phantom_s1`, `muzzle_silencer_sg`, `muzzle_silencer_ursus`, `muzzle_silencer_vulture`, `laser_peq6`, `stock_heavy_spas_12`, `stock_tactical_ar`, `sight_552`, `sight_exp3`, `sight_uh1`, high-tier mags
- **t5** — precision optics / golden parts: `scope_mk5hd`, `deagle_golden_long_barrel`, `sniper_extended_mag_2`
- **t6** — endgame: `sniper_extended_mag_3`

## Editing the mapping

All tier decisions live in one file: `data/tier-map.json`. Edit the tier
(th `t1`..`t7`) for any recipe id, then re-run:

```
.\scripts\generate-recipes.ps1
.\build.ps1
```

## Crafting-station blocks
The TACZ-built-in block recipes (crafted in the vanilla table) use vanilla iron. These are all re-tiered to **t1 Wrought Iron** (foundational infrastructure, not tiered like weapons):

| Recipe | What changed |
|--------|--------------|
| \gun_smith_table\ | \orge:ingots/iron\ -> \	fc:metal/ingot/wrought_iron\; \minecraft:iron_block\ -> \	fc:metal/block/wrought_iron\ |
| \iron_ammo_box\ | \orge:ingots/iron\ -> \	fc:metal/ingot/wrought_iron\ |
| \ttachment_workbench\ | \orge:ingots/iron\ -> \	fc:metal/ingot/wrought_iron\ |
| \mmo_workbench\ | \orge:ingots/iron\ -> \	fc:metal/ingot/wrought_iron\ |
| \	arget\ | \orge:ingots/iron\ -> \	fc:metal/ingot/wrought_iron\ |

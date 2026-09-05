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
| t7 | Blue Steel | `tfc:metal/ingot/blue_steel` | End-game (ex-stock netherite requirement) |

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

> `m107` / `m95` / `minigun` carry a separate `forge:ingots/netherite` line in
> their stock recipes; that line maps to blue steel (`tfc:metal/ingot/blue_steel`).

## Ammunition

All 24 ammo recipes are tiered and copper-cased, so their brass ingots follow:

- **t1** — `12g`, `9mm`, `45acp`, `22wmr`, `762x25`, `357mag`
- **t2** — `40mm`, `30_06`, `308`, `45_70`, `762x39`, `762x54`, `46x30`, `57x28`
- **t3** — `rpg_rocket`, `556x45`, `545x39`, `792x57`, `500mag`, `50ae`, `338`, `58x42`, `68x51fury`
- **t4** — `50bmg`

Ammo cases use `tfc:metal/ingot/brass`; the lapis-tinted tracer ammo uses `tfc:metal/ingot/bismuth`.

## Attachments

Iron-using attachments are tiered by mechanical complexity and cost:

- **t1** — simple fixings: `bayonet_6h3`, `bayonet_m9`, `grip_rk6`, `oem_stock_light`, small reflex dots, `stock_m4ss`, `laser_nightstick`
- **t2** — grips / basic rails / pistols dots / basic stock: `grip_*`, `laser_compact`, `laser_lopro`, `oem_stock_heavy`, `oem_stock_tactical`, `scope_1873_6x`, `scope_retro_2x`, `sight_coyote`, `sight_okp7`, `sight_srs_02`, `stock_moe`, `stock_ripstock`, `stock_sba3`, `stock_carbon_bone_c5`, low-tier mags, `muzzle_brake_cyclone_d2`, `muzzle_compensator_trident`, `muzzle_silencer_mirage`, `muzzle_silencer_wraith`
- **t3** — locks / mid scopes: `scope_98k`, `scope_contender`, `muzzle_brake_pioneer`, `muzzle_brake_timeless50`, `stock_ak12`, `stock_hk_slim_line`, `stock_militech_b5`, `muzzle_silencer_ptilopsis`, mid-tier mags, `stock_tactical_spas_12`
- **t4** — modern optics / advanced muzzles: `scope_acog_ta31`, `scope_elcan_4x`, `scope_hamr`, `scope_lpvo_1_6`, `scope_qmk152`, `scope_standard_8x`, `scope_vudu`, `muzzle_brake_cthulhu`, `muzzle_brake_mastiff_sg`, `muzzle_brake_trex`, `muzzle_choke_sg`, `muzzle_silencer_knight_qd`, `muzzle_silencer_phantom_s1`, `muzzle_silencer_sg`, `muzzle_silencer_ursus`, `muzzle_silencer_vulture`, `laser_peq6`, `stock_heavy_spas_12`, `stock_tactical_ar`, `sight_552`, `sight_exp3`, `sight_uh1`, high-tier mags
- **t5** — precision optics / golden parts: `scope_mk5hd`, `deagle_golden_long_barrel`, `sniper_extended_mag_2`
- **t6** — endgame: `sniper_extended_mag_3`

Ammo modifiers (`ammo_mod_*`) are also tiered:
- **t2** — `ammo_mod_fmj`
- **t3** — `ammo_mod_hp`, `ammo_mod_slug`
- **t4** — `ammo_mod_he`, `ammo_mod_i`

Their crying-obsidian base maps to `tfc:metal/ingot/black_bronze`; the `i` mod's
blaze rod maps to `tfc:metal/rod/red_steel`; the `slug` mod's netherite scrap
line maps to `tfc:metal/ingot/unknown`.

## Non-iron ingredient substitutions

Beyond the tier metal, stock TACZ ingredients that TFC cannot resolve are
replaced. These apply in `RecipeTransformer.transformMaterials` and are
mirrored in `scripts/generate-recipes.ps1`:

| Stock ingredient | Replacement |
|------------------|-------------|
| `forge:gems/amethyst` | tag `tacz_tfc_integration:cut_gems` (9 TFC cut gems) |
| `forge:gems/quartz` | `tfc:metal/ingot/weak_steel` |
| `forge:rods/blaze` | `tfc:metal/rod/red_steel` |
| `forge:gems/lapis` (guns) | `tfc:metal/ingot/nickel` |
| `forge:gems/lapis` (ammo) | `tfc:metal/ingot/bismuth` |
| `forge:gems/lapis` (attachments) | `tfc:metal/ingot/sterling_silver` |
| `forge:ingots/copper` (ammo) | `tfc:metal/ingot/brass` |
| `minecraft:crying_obsidian` (attachments) | `tfc:metal/ingot/black_bronze` |
| `minecraft:ancient_debris` (attachments) | `tfc:metal/ingot/unknown` |
| `forge:ingots/netherite` | `tfc:metal/ingot/blue_steel` |
| `forge:ores/netherite_scrap` | `tfc:metal/ingot/unknown` |

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

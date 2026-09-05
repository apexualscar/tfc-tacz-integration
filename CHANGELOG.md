# Changelog

## [Unreleased]

- Fix missing shotgun shell ammo: tier `12g` at t1 (wrought iron nuggets) so all
  shotgun ammunition is craftable in TFC.
- Fix runtime generator reading TACZ recipes: read the unpacked
  `tacz/tacz_default_gun` pack instead of a jar path that does not exist in
  current TACZ builds (1.1.4/1.1.8). Overrides now generate on fresh installs.
- Fix block recipes: read and re-tier TACZ's own block recipes
  (`gun_smith_table`, `iron_ammo_box`, `attachment_workbench`, `ammo_workbench`,
  `target`), preserving correct results/NBT, and write them into the override
  gunpack instead of a dead folder.
- Initial repository scaffold
- Override gunpack skeleton (`build/00_tacz_tfc_progression`)
- Tier mapping documentation
- Fix gunpack load precedence: name override `zz_` so it enumerates after
  `tacz_default_gun` and wins TACZ's last-wins recipe merge.
- Re-tier TACZ crafting-station block recipes (gun smith table, iron ammo box,
  attachment table, ammo workbench, target) to wrought iron.
- Convert from a manual gunpack to a Forge mod jar. Generates the override
  gunpack on startup, reads TACZ's current recipes, and picks up new guns on
  TACZ updates.
- Add player-facing config: `tier-metals.toml` (tier to metal) and
  `recipe-tiers.toml` (recipe to tier), copied into `config/` on first run.

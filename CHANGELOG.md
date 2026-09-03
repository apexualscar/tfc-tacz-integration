# Changelog

## [Unreleased]

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

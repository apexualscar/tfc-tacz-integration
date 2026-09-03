# Changelog

## [Unreleased]

- Initial repository scaffold
- Override gunpack skeleton (`build/00_tacz_tfc_progression`)
- Tier mapping documentation
- Fix gunpack load precedence: rename override folder to `00_` so it
  enumerates before `tacz_default_gun` (TACZ resolves recipes first-wins).

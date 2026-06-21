# Changelog

Versions follow the format **`<Minecraft_Version>-<Major>.<Minor>.<Feature>.<Patch>`**, with optional pre-release tags.

| Segment | Incremented for |
| :-- | :-- |
| **Major** | Complete rewrites |
| **Minor** | New Minecraft patch support, large features, or save-breaking changes |
| **Feature** | Added or reworked features that keep compatibility |
| **Patch** | Small bug fixes and optimizations |

**Pre-release order** (earliest → stable):
`-a#` alpha → `-b#` beta → `-rc#` release candidate → stable

> **Example:**
> `1.20.1-2.0.0.0-a1` < `1.20.1-2.0.0.0-b1` < `1.20.1-2.0.0.0-rc1` < `1.20.1-2.0.0.0-rc2` < `1.20.1-2.0.0.0`

---

## [1.20.1-0.2.0.0] — 2026-06-21

### Added
- Sound on successful vial use
- Poison tincture fluid for creating vials with vanilla poison effect

### Changed

- Renamed:
  - Greater Poison (fluid) → Necrotoxin
  - Greater Poison (effect) → Necrosis
  - Vial of Greater Poison → Vial of Necrotoxin
- Necrotoxin requires 1000 mB of milk (was 500 mB)
- Recolored antidote fluid to better match mean item texture liquid color
- Recolored necrotoxin fluid and effect to better match mean placeholder effect texture color

### Fixed

- Fixed poison vial strings calling the fluid "greater poison" instead of "vial of poison"
- Fixed missing translation key on the greater poison vial
- Fixed untranslatable poisoned tooltip in creative mode

### Technical

- **Breaking:** ID:
  - Fluid toxicologistsdelight:acid → toxicologistsdelight:necrotoxin
  - Fluid toxicologistsdelight:cure → toxicologistsdelight:antidote
  - Mob effect toxicologistsdelight:acid → toxicologistsdelight:necrotoxin
  - Mob effect toxicologistsdelight:cure → toxicologistsdelight:antidote
  - Item toxicologistsdelight:acid_vial → toxicologistsdelight:necrotoxin_vial
  - Item toxicologistsdelight:cure_vial → toxicologistsdelight:antidote_vial
- Localisation message IDs:
  - Added gui.toxicologistsdelight.poisoned
  - Renamed toxicologistsdelight.acid → toxicologistsdelight.necrotoxin
  - Renamed toxicologistsdelight.cure → toxicologistsdelight.antidote

## [1.20.1-0.1.0.0] — 2026-06-21

### Added

- Mob Effects:
  - Greater Poison
- Items:
  - Glass Vial
  - Vial of Poison
  - Vial of Greater Poison
  - Vial of Antidote
- Fluids:
  - Greater Poison
  - Antidote
- Recipes:
  - Glass Vial (crafting)
  - Vial of Poison (fermenting)
  - Vial of Greater Poison (fermenting)
  - Vial of Antidote (cooking)
  - Vial of Poison (pouring)
  - Vial of Greater Poison (pouring)
  - Vial of Antidote (pouring)
- Mechanics:
  - Food poisoning
  - Potion poisoning
  - Food poison cleansing
  - Potion poison cleansing
- Translations:
  - Russian locale

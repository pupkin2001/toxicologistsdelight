# Changelog

Versions follow the format **`<Minecraft_Version>-v<Major>.<Minor>.<Feature>.<Patch>`**, with optional pre-release tags.

*Note: Releases before v0.4.0.0 omitted **`v`** before version numbers.*

| Segment     | Incremented for                                                         |
|:------------|:------------------------------------------------------------------------|
| **Major**   | Complete rewrites                                                       |
| **Minor**   | New Minecraft version support, large features, or save-breaking changes |
| **Feature** | Added or reworked features that keep compatibility                      |
| **Patch**   | Small bug fixes and optimizations                                       |

**Pre-release order** (earliest → stable):
`-a#` alpha → `-b#` beta → `-rc#` release candidate → stable

> **Example:**
> `1.20.1-2.0.0.0-a1` < `1.20.1-2.0.0.0-b1` < `1.20.1-2.0.0.0-rc1` < `1.20.1-2.0.0.0-rc2` < `1.20.1-2.0.0.0`

---

## [0.3.0.0] — 2026-10-26

### Added

- Tincture of Natural Vitality (Regeneration)
- Poisonable Farmer's Delight feasts and pies
- Vial of Antidote can be drunk to cure Necrosis, Rot and Poison
- Another dose of harmful poison raises its strength (up to strength III), except Natural Vitality
- Grace period before necrosis starts dealing damage
- Drinks without food values (milk buckets, Farmer's Delight milk bottles, hot cocoa and melon juice) can be poisoned
- Putrescine from lily pads recipe

### Changed

- Necrosis, Rot and Toxic damage no longer scales with difficulty
- Necrosis cannot be cured by milk
- Necrosis never shows effect particles
- Necrosis no longer bypasses resistance
- Putrescine accepts any cattail in `#forge:cattail`
- Limit amount of damage dealt in quick succession by rot
- Hearts stay still during the necrotoxin grace period and shake harder as the damage ramps up

### Fixed

- Patched [dupe present in Brewin' and Chewin' 3.2.0+](https://github.com/Monad-Modding/BrewinAndChewin/issues/42):
  - Brewin' and Chewin' kegs deleting item results that don't fit the output slot
  - Brewin' and Chewin' 3.2 keg dupe: pouring filled more containers than the keg held, and pouring in used up drinks
    that didn't fit
- Armour bar drawing on top of health bar when necrosis is active
- Health bar not flashing outlines or rendering special textures (poisoned/withered) when necrosis is active
- Animals being poisoned by food they didn't eat (adults already in love or on breeding cooldown)
- Tamed wolves not being poisoned by poisoned Farmer's Delight dog food
- Using up the last vial in stack sometimes deleting the empty vial it gives back
- Splash and lingering potions being poisonable
- Furnaces and cooking pots merging poisoned and clean results
- Mod resources being flagged as an incompatible pack
- Poison tincture missing display item in keg UI

### Technical

- Requires Forge 47.3.30, Farmer's Delight 1.2.5 and Brewin' and Chewin' 3.0.0 or newer
- Mixins: refmap renamed to `mixins.toxicologistsdelight.refmap.json`
- Fluids carry poison under the same NBT keys as items
- Network channel `toxicologistsdelight:main`, syncing Necrosis ramp
- Necrosis hearts shake the vanilla health bar instead of replacing it

## [0.2.2.0] — 2026-06-28

### Added

- Necrosis damage ramping up with time
- Recipe book entries for every recipe
- Creative tab registration for every item

### Fixed

- Necrosis effect displaying missing texture instead of its icon

## [0.2.1.1] — 2026-06-27

### Added

- Русскій дореформенный (rpr) locale

### Fixed

- Particle cloud now takes main hand skin customisation setting into account when spawning
- Missing poison tincture localisation strings
- Missing poison tincture display item in keg UI

## [0.2.1.0] — 2026-06-26

### Added

- Particle cloud near offhand on successful vial use

### Changed

- Milk bucket in antidote recipe can now be replaced by equivalent items from other mods if they are tagged as
  <code>#forge:milk/milk</code>
- Fermenting recipe costs:
  - Necrotoxin (Added red mushroom)
  - Antidote (Added honey bottle, charcoal, sweet berries)

### Fixed

- Game crashing due to missing registry entries when forge decides to load Brewin' and Chewin' or Farmer's Delight after
  Toxicologists's Delight
- All vial fermenting recipes never being able to start and confusing JEI
- Necrosis damage being affected by damage reduction

### Technical

- 'acid' damage type renamed to 'necrotoxin'

## [0.2.0.0] — 2026-06-21

### Added

- Sounds on successful vial use
- Poison tincture fluid for creating vials with vanilla poison effect

### Changed

- Renamed Greater Poison:
  - Greater Poison (fluid) → Necrotoxin
  - Greater Poison (effect) → Necrosis
  - Vial of Greater Poison → Vial of Necrotoxin
- Necrotoxin recipe requires 1000 mB of milk (was 500 mB)
- Antidote fluid recoloured to better match mean item texture liquid colour
- Necrotoxin fluid and effect recoloured to better match mean placeholder effect texture colour

### Fixed

- Poison vial strings calling the fluid "greater poison" instead of "vial of poison"
- Missing translation key on the greater poison vial
- Poisoned tooltip in creative mode missing localisation string

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

## [0.1.0.0] — 2026-06-21

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

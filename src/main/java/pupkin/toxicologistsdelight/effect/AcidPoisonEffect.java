package pupkin.toxicologistsdelight.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

import java.util.function.Consumer;

public class AcidPoisonEffect extends MobEffect
{
	
	private static final ResourceKey<DamageType> ACID_DAMAGE_KEY =
			
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   
			                   new ResourceLocation(ToxicologistsDelight.MOD_ID, "acid"));
	
	
	
	public AcidPoisonEffect(MobEffectCategory category, int color) {
		
		super(category, color);
		
	}
	
	
	
	@Override
	
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		
		if (!entity.level().isClientSide) {
			
			float damage = 2.0F + 2 * amplifier;
			
			
			
			// Get the registered DamageType holder
			
			Holder<DamageType> acidType = entity.level().registryAccess()
			                                    
			                                    .registryOrThrow(Registries.DAMAGE_TYPE)
			                                    
			                                    .getHolderOrThrow(ACID_DAMAGE_KEY);
			
			
			
			// Public constructor: DamageSource(Holder<DamageType>, @Nullable Entity directEntity, @Nullable Entity causingEntity)
			
			DamageSource acidSource = new DamageSource(acidType, null, null);
			
			
			
			entity.hurt(acidSource, damage);
			
		}
		
	}
	
	
	
	@Override
	
	public boolean isDurationEffectTick(int duration, int amplifier) {
		
		int interval = 25 >> amplifier;
		
		if (interval > 0) return duration % interval == 0;
		
		return true;
		
	}
	
	
	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new IClientMobEffectExtensions() {
			// You will need two textures to accommodate standard and compact UI modes
//			private static final ResourceLocation ACID_BG_NORMAL =
//					new ResourceLocation(ToxicologistsDelight.MOD_ID, "textures/gui/acid_effect_bg_normal.png");
//			private static final ResourceLocation ACID_BG_COMPACT =
//					new ResourceLocation(ToxicologistsDelight.MOD_ID, "textures/gui/acid_effect_bg_compact.png");
			
			// TODO: Broken, fix rendering later
			// 1. Draw the background, then the icon
//			@Override
//			public boolean renderInventoryIcon(MobEffectInstance instance,
//			                                   EffectRenderingInventoryScreen<?> screen,
//			                                   GuiGraphics guiGraphics, int x, int y, int blitOffset) {
//				var mc = screen.getMinecraft();
//
//				// --- BACKGROUND OVERRIDE ---
//				// Vanilla determines if the UI is compact by shifting the X coordinate.
//				// If the distance from the left edge of the GUI to the effect box is <= 32, it's compact.
//				boolean isCompact = (screen.getGuiLeft() - x) <= 32;
//
//				// Draw your custom texture over the vanilla one.
//				// Note: Your custom texture should be fully opaque to hide the vanilla box underneath!
//				if (isCompact) {
//					guiGraphics.blit(ACID_BG_COMPACT, x, y, 0, 0, 32, 32, 32, 32);
//				} else {
//					guiGraphics.blit(ACID_BG_NORMAL, x, y, 0, 0, 120, 32, 120, 32);
//				}
//
//				// --- ICON RENDERING ---
//				TextureAtlasSprite sprite = mc.getMobEffectTextures().get(AcidPoisonEffect.this);
//
//				// Tip: In Vanilla 1.20.1, icons are actually drawn at (x + 6, y + 7).
//				// I updated it here so your icon aligns perfectly with vanilla effects.
//				guiGraphics.blit(x + 6, y + 7, 0, 18, 18, sprite);
//
//				return true; // Blocks the default vanilla icon from rendering over yours
//			}
			
			// 2. Keep the effect name, replace the duration with "Eternal"
			@Override
			public boolean renderInventoryText(MobEffectInstance instance,
			                                   EffectRenderingInventoryScreen<?> screen,
			                                   GuiGraphics guiGraphics, int x, int y, int blitOffset) {
				var mc = screen.getMinecraft();
				
				// Draw the effect's usual name (white)
				guiGraphics.drawString(mc.font,
				                       instance.getEffect().getDisplayName(),
				                       x + 10 + 18, y + 6,
				                       0xFFFFFF, true); // Added drop shadow for readability
				
				// Draw "Eternal" in gray where the duration normally appears (10px below)
				guiGraphics.drawString(mc.font,
				                       net.minecraft.client.resources.language.I18n.get("gui.toxicologistsdelight.eternal"),
				                       x + 10 + 18, y + 6 + 10,
				                       0x7F7F7F, true);
				
				return true; // Suppress the default vanilla name/duration text
			}
		});
	}
}
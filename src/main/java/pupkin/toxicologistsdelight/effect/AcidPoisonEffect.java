package pupkin.toxicologistsdelight.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
	
	public AcidPoisonEffect() { super(MobEffectCategory.HARMFUL, 0x62196d); }
	
	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier)
	{
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
	public boolean isDurationEffectTick(int duration, int amplifier)
	{
		int interval = 25 >> amplifier;
		if (interval > 0) return duration % interval == 0;
		return true;
	}
	
	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer)
	{
		consumer.accept(new IClientMobEffectExtensions()
		{
			@Override
			public boolean renderInventoryText(MobEffectInstance instance,
			                                   EffectRenderingInventoryScreen<?> screen,
			                                   GuiGraphics guiGraphics, int x, int y, int blitOffset)
			{
				var mc = screen.getMinecraft();
				
				// Draw the effect's name
				guiGraphics.drawString(mc.font,
				                       Component.translatable("potion.withAmplifier", instance.getEffect().getDisplayName(),
				                                              Component.translatable("potion.potency." + instance.getAmplifier())),
				                       x + 10 + 18, y + 6,
				                       0xAA0000, true);
				
				// Draw "Eternal" where the duration normally appears
				guiGraphics.drawString(mc.font,
				                       net.minecraft.client.resources.language.I18n.get("gui.toxicologistsdelight.eternal"),
				                       x + 10 + 18, y + 6 + 10,
				                       0x7F7F7F, true);
				
				return true; // Suppress the default vanilla name/duration text
			}
		});
	}
}

package pupkin.toxicologistsdelight.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

public class RotPoisonEffect extends MobEffect
{
	public RotPoisonEffect()
	{
		super(MobEffectCategory.HARMFUL, 0x4B0082);
	}
	
	private static final ResourceKey<DamageType> ROT_DAMAGE_KEY =
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   new ResourceLocation(ToxicologistsDelight.MOD_ID, "rot"));
	
	
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ReverseHealingHandler
	{
		@SubscribeEvent
		public static void onLivingHeal(LivingHealEvent event)
		{
			LivingEntity entity = event.getEntity();
			
			// Only affect entities that have the effect
			if (!entity.hasEffect(ToxicologistsEffects.ROT.get())) return;
			
			float healAmount = event.getAmount();
			event.setCanceled(true);
			
			Holder<DamageType> rotType = entity.level().registryAccess()
			                                   .registryOrThrow(Registries.DAMAGE_TYPE)
			                                   .getHolderOrThrow(ROT_DAMAGE_KEY);
			
			DamageSource rotSource = new DamageSource(rotType, null, null);
			
			entity.hurt(rotSource, healAmount);
		}
	}
}
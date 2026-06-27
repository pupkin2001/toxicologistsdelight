package pupkin.toxicologistsdelight.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID)
public final class NecrotoxinRampHandler
{
	@SubscribeEvent
	public static void onLivingTick(LivingEvent.LivingTickEvent event)
	{
		LivingEntity e = event.getEntity();
		if (e.level().isClientSide()) return;
		if (!e.hasEffect(ToxicologistsEffects.NECROTOXIN.get())) return;
		
		CompoundTag data = e.getPersistentData();
		long ticks = data.getLong(NecrotoxinPoisonEffect.RAMP_TICKS_KEY);
		if (ticks < NecrotoxinPoisonEffect.RAMP_TICK_CAP) {
			data.putLong(NecrotoxinPoisonEffect.RAMP_TICKS_KEY, ticks + 1L);
		}
	}
	
	@SubscribeEvent
	public static void onRemoved(MobEffectEvent.Remove event)
	{
		clearIfNecrotoxin(event.getEntity(), event.getEffect());
	}
	
	@SubscribeEvent
	public static void onExpired(MobEffectEvent.Expired event)
	{
		assert event.getEffectInstance() != null;
		clearIfNecrotoxin(event.getEntity(), event.getEffectInstance().getEffect());
	}
	
	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event)
	{
		// Players already lose top-level persistent data on respawn; this covers mobs and any datapack quirks.
		event.getEntity().getPersistentData().remove(NecrotoxinPoisonEffect.RAMP_TICKS_KEY);
	}
	
	private static void clearIfNecrotoxin(LivingEntity e, MobEffect effect)
	{
		if (e.level().isClientSide()) return;
		if (effect != ToxicologistsEffects.NECROTOXIN.get()) return;
		e.getPersistentData().remove(NecrotoxinPoisonEffect.RAMP_TICKS_KEY);
	}
}
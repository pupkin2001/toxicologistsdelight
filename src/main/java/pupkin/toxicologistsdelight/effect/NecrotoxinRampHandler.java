package pupkin.toxicologistsdelight.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.network.NecrosisIntensityPacket;
import pupkin.toxicologistsdelight.network.ToxicologistsNetwork;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID)
public final class NecrotoxinRampHandler
{
	private static final int SYNC_INTERVAL = 10;
	
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
		// The counter lives on the server only; the poisoned player's client needs it to shake the hearts.
		if (e instanceof ServerPlayer player && player.tickCount % SYNC_INTERVAL == 0) {
			ToxicologistsNetwork.sendTo(player, new NecrosisIntensityPacket(NecrotoxinPoisonEffect.rampIntensity(player)));
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
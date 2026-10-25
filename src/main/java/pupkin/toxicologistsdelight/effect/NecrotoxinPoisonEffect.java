package pupkin.toxicologistsdelight.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NecrotoxinPoisonEffect extends MobEffect
{
	/** Persistent-data key - how many ticks the effect has been active on this entity. */
	public static final String RAMP_TICKS_KEY = ToxicologistsDelight.MOD_ID + ":necrotoxin_ramp_ticks";
	/** Time, in seconds at strength 1, after poisoning before any damage is dealt. The ramp starts once it ends. */
	private static final float GRACE_PERIOD_SECONDS = 30.0F;
	/** Time, in seconds at strength 1, for damage to ramp from 0 up to the ceiling. */
	private static final float RAMP_UP_TIME_SECONDS = 120.0F;
	/** Counter stops here (the most that any strength could need, i.e. the strength-1 grace period + ramp length). */
	public static final long RAMP_TICK_CAP = (long) Math.ceil((GRACE_PERIOD_SECONDS + RAMP_UP_TIME_SECONDS) * 20.0F) + 1L;
	/** Damage ceiling. */
	private static final float MAX_DAMAGE_PER_TICK = 8.0F;
	/** Curve. 1.0 = linear. >1 = slow start / fast finish. <1 = fast start. */
	private static final float RAMP_EXPONENT = 1.0F;
	private static final ResourceKey<DamageType> NECROTOXIN_DAMAGE_KEY =
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "necrotoxin"));
	
	public NecrotoxinPoisonEffect() { super(MobEffectCategory.HARMFUL, 0x703a76); }
	
	// Only the antidote cures it - milk (and Farmer's Delight milk bottles / hot cocoa, which ask about milk) no longer does.
	@Override
	public List<ItemStack> getCurativeItems()
	{
		List<ItemStack> curativeItems = new ArrayList<>();
		curativeItems.add(new ItemStack(ToxicologistsItems.ANTIDOTE_VIAL.get()));
		return curativeItems;
	}
	
	private static int ticksPerSecondFor(int amplifier)
	{
		// amp 0->20, 1->18, 2->16 ... clamped so amp>=10 never divides by zero.
		return Math.max(1, 20 - 2 * amplifier);
	}
	
	private static long graceTicksFor(int amplifier)
	{
		return (long) (GRACE_PERIOD_SECONDS * ticksPerSecondFor(amplifier));
	}
	
	private static float rampedDamage(long elapsedTicks, int amplifier)
	{
		float rampTicks = RAMP_UP_TIME_SECONDS * ticksPerSecondFor(amplifier);
		if (rampTicks <= 0.0F) return MAX_DAMAGE_PER_TICK;
		float progress = Mth.clamp(elapsedTicks / rampTicks, 0.0F, 1.0F);
		if (RAMP_EXPONENT != 1.0F) progress = (float) Math.pow(progress, RAMP_EXPONENT);
		return MAX_DAMAGE_PER_TICK * progress;
	}
	
	/** 0 until the grace period ends, then how far the damage has ramped up, up to 1. Server side only: the tick counter isn't synced. */
	public static float rampIntensity(LivingEntity entity)
	{
		MobEffectInstance necrosis = entity.getEffect(ToxicologistsEffects.NECROTOXIN.get());
		if (necrosis == null) return 0.0F;
		long elapsed = entity.getPersistentData().getLong(RAMP_TICKS_KEY);
		long graceTicks = graceTicksFor(necrosis.getAmplifier());
		if (elapsed < graceTicks) return 0.0F;
		return rampedDamage(elapsed - graceTicks, necrosis.getAmplifier()) / MAX_DAMAGE_PER_TICK;
	}
	
	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier)
	{
		if (!entity.level().isClientSide) {
			long elapsed = entity.getPersistentData().getLong(RAMP_TICKS_KEY);
			long graceTicks = graceTicksFor(amplifier);
			if (elapsed < graceTicks) return;
			float damage = rampedDamage(elapsed - graceTicks, amplifier);
			
			Holder<DamageType> necrotoxinType = entity.level().registryAccess()
			                                          .registryOrThrow(Registries.DAMAGE_TYPE)
			                                          .getHolderOrThrow(NECROTOXIN_DAMAGE_KEY);
			DamageSource necrotoxinSource = new DamageSource(necrotoxinType, null, null);
			entity.hurt(necrotoxinSource, damage);
		}
	}
	
	@Override
	public boolean isDurationEffectTick(int duration, int amplifier)
	{
		int interval = 200 >> amplifier;
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
				
				guiGraphics.drawString(mc.font,
				                       Component.translatable("potion.withAmplifier", instance.getEffect().getDisplayName(),
				                                              Component.translatable("potion.potency." + instance.getAmplifier())),
				                       x + 10 + 18, y + 6,
				                       0xAA0000, true);
				
				guiGraphics.drawString(mc.font,
				                       Component.translatable("gui.toxicologistsdelight.eternal"),
				                       x + 10 + 18, y + 6 + 10,
				                       0x7F7F7F, true);
				
				return true;
			}
		});
	}
	
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class HideParticlesHandler
	{
		// Recolour the swirl particles as if necrotoxin wasn't there, no matter how the effect was applied.
		// Highest priority so other mods' listeners still get to adjust the result.
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void onPotionColorCalculation(PotionColorCalculationEvent event)
		{
			MobEffect necrotoxin = ToxicologistsEffects.NECROTOXIN.get();
			List<MobEffectInstance> others = event.getEffects().stream()
			                                      .filter(instance -> instance.getEffect() != necrotoxin)
			                                      .toList();
			if (others.size() == event.getEffects().size()) return;
			
			// getColor() returns the water colour for an empty list, which would still spawn particles; 0 spawns none.
			event.setColor(others.isEmpty() ? 0 : PotionUtils.getColor(others));
			event.shouldHideParticles(LivingEntity.areAllEffectsAmbient(others));
		}
	}
}

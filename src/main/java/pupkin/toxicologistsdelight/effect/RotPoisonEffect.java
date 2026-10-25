package pupkin.toxicologistsdelight.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

import java.util.ArrayList;
import java.util.List;

public class RotPoisonEffect extends MobEffect
{
	/** How much healing rot may turn into damage */
	public static final String BUDGET_KEY = ToxicologistsDelight.MOD_ID + ":rot_budget";
	/** Budget gained per second of the effect, per level */
	private static final float BUDGET_PER_SECOND = 0.25F;
	/** Budget gained per level when applying the effect */
	private static final float BUDGET_PER_DOSE = 2.0F;
	/** Budget cap, per level */
	private static final float MAX_BUDGET = 6.0F;
	private static final ResourceKey<DamageType> ROT_DAMAGE_KEY =
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "rot"));
	
	public RotPoisonEffect()
	{
		super(MobEffectCategory.HARMFUL, 0x4B0082);
	}
	
	private static void addBudget(LivingEntity entity, float amount, int amplifier)
	{
		CompoundTag data = entity.getPersistentData();
		float max = MAX_BUDGET * (amplifier + 1);
		data.putFloat(BUDGET_KEY, Math.min(max, data.getFloat(BUDGET_KEY) + amount * (amplifier + 1)));
	}
	
	@Override
	public List<ItemStack> getCurativeItems()
	{
		List<ItemStack> curativeItems = new ArrayList<>();
		curativeItems.add(new ItemStack(Items.MILK_BUCKET));
		curativeItems.add(new ItemStack(ToxicologistsItems.ANTIDOTE_VIAL.get()));
		return curativeItems;
	}
	
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ReverseHealingHandler
	{
		@SubscribeEvent
		public static void onLivingHeal(LivingHealEvent event)
		{
			LivingEntity entity = event.getEntity();
			
			if (!entity.hasEffect(ToxicologistsEffects.ROT.get())) return;
			
			float healAmount = event.getAmount();
			event.setCanceled(true);
			if (entity.level().isClientSide()) return;
			
			CompoundTag data = entity.getPersistentData();
			float damage = Math.min(healAmount, data.getFloat(BUDGET_KEY));
			if (damage <= 0.0F) return;
			data.putFloat(BUDGET_KEY, data.getFloat(BUDGET_KEY) - damage);
			
			Holder<DamageType> rotType = entity.level().registryAccess()
			                                   .registryOrThrow(Registries.DAMAGE_TYPE)
			                                   .getHolderOrThrow(ROT_DAMAGE_KEY);
			
			DamageSource rotSource = new DamageSource(rotType, null, null);
			
			entity.hurt(rotSource, damage);
		}
		
		@SubscribeEvent
		public static void onLivingTick(LivingEvent.LivingTickEvent event)
		{
			LivingEntity entity = event.getEntity();
			if (entity.level().isClientSide()) return;
			MobEffectInstance rot = entity.getEffect(ToxicologistsEffects.ROT.get());
			if (rot != null) addBudget(entity, BUDGET_PER_SECOND / 20.0F, rot.getAmplifier());
		}
		
		@SubscribeEvent
		public static void onAdded(MobEffectEvent.Added event)
		{
			MobEffectInstance added = event.getEffectInstance();
			if (event.getEntity().level().isClientSide() || added.getEffect() != ToxicologistsEffects.ROT.get()) return;
			addBudget(event.getEntity(), BUDGET_PER_DOSE, added.getAmplifier());
		}
		
		@SubscribeEvent
		public static void onRemoved(MobEffectEvent.Remove event)
		{
			if (event.getEffect() == ToxicologistsEffects.ROT.get()) event.getEntity().getPersistentData().remove(BUDGET_KEY);
		}
		
		@SubscribeEvent
		public static void onExpired(MobEffectEvent.Expired event)
		{
			MobEffectInstance expired = event.getEffectInstance();
			if (expired != null && expired.getEffect() == ToxicologistsEffects.ROT.get()) event.getEntity().getPersistentData().remove(BUDGET_KEY);
		}
		
		@SubscribeEvent
		public static void onDeath(LivingDeathEvent event)
		{
			event.getEntity().getPersistentData().remove(BUDGET_KEY);
		}
	}
}

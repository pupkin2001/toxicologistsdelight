package pupkin.toxicologistsdelight.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

import java.util.function.Consumer;

public class ToxicosisEffect extends MobEffect // ! Both toxicosis and toxic crisis effects live here.
{
	private static final ResourceKey<DamageType> TOXICOSIS_DAMAGE_KEY =
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "toxicosis"));
	private static final ResourceKey<DamageType> TOXIC_CRISIS_DAMAGE_KEY =
			ResourceKey.create(Registries.DAMAGE_TYPE,
			                   ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "toxic_crisis"));
	
	public ToxicosisEffect() { super(MobEffectCategory.HARMFUL, 0x4B0082); }
	
	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) { return false; }
	
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
				
				// Draw the effect's usual name
				guiGraphics.drawString(mc.font,
				                       instance.getEffect().getDisplayName(),
				                       x + 10 + 18, y + 6,
				                       0x00AA00, true);
				
				// Draw progress where the duration normally appears
				guiGraphics.drawString(mc.font,
				                       "60%", // TODO
				                       x + 10 + 18, y + 6 + 10,
				                       0x557755, true);
				
				return true; // Suppress the default vanilla name/duration text
			}
		});
	}
	
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ToxicosisHealingHandler
	{
		// Flag entities about to naturally regen
		@SubscribeEvent
		public static void onLivingTick(LivingEvent.LivingTickEvent event)
		{
			LivingEntity entity = event.getEntity();
			if (!(entity instanceof Player player)) return;
			if (!player.hasEffect(ToxicologistsEffects.TOXICOSIS.get()) ||
					!player.hasEffect(ToxicologistsEffects.TOXIC_CRISIS.get())) { return; }
			
			// Mirror vanilla's regen condition (from Player.java)
			boolean saturatedRegen = player.getFoodData().getSaturationLevel() > 0
					&& player.isHurt()
					&& player.getFoodData().getFoodLevel() >= 20;
			
			boolean normalRegen = player.getFoodData().getFoodLevel() >= 18
					&& player.isHurt();
			
			if (saturatedRegen || normalRegen) {
				// Tag "about to naturally regen this tick"
				player.getPersistentData().putBoolean("pending_natural_regen", true);
			}
		}
		
		@SubscribeEvent
		public static void onLivingHeal(LivingHealEvent event)
		{
			LivingEntity entity = event.getEntity();
			if (!entity.hasEffect(ToxicologistsEffects.TOXICOSIS.get()) ||
					!entity.hasEffect(ToxicologistsEffects.TOXIC_CRISIS.get())) { return; }
			
			if (entity.getPersistentData().getBoolean("pending_natural_regen")) {
				entity.getPersistentData().remove("pending_natural_regen");
				event.setCanceled(true);
			}
		}
	}
}

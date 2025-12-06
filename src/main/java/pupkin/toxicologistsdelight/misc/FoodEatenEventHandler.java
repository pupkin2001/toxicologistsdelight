package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FoodEatenEventHandler
{
	@SubscribeEvent
	public static void onFoodEaten(LivingEntityUseItemEvent.@NotNull Finish event)
	{
		if (event.getEntity() instanceof Player player &&
				event.getItem().isEdible() &&
				PoisonUtils.isPoisoned(event.getItem())) {
			
			int strength = PoisonUtils.getPoisonStrength(event.getItem());
			player.addEffect(new MobEffectInstance(
					MobEffects.POISON,
					strength * 600,
					strength - 1
			));
		}
	}
}

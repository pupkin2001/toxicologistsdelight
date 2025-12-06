package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.List;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemUsedEventHandler
{
	
	@SubscribeEvent
	public static void onItemUsed(LivingEntityUseItemEvent.@NotNull Finish event)
	{
		if (!(event.getEntity() instanceof Player player)) return;
		
		ItemStack usedItem = event.getItem();
		
		if (!PoisonUtils.isPoisoned(usedItem)) return;
		
		int strength = PoisonUtils.getPoisonStrength(usedItem);
		
		if (usedItem.isEdible()) {
			player.addEffect(new MobEffectInstance(
					MobEffects.POISON,
					strength * 600,
					strength - 1
			));
		} else if (usedItem.getItem() instanceof PotionItem) {
			List<MobEffectInstance> existingEffects = PotionUtils.getMobEffects(usedItem);
			
			MobEffectInstance poisonEffect = new MobEffectInstance(
					MobEffects.POISON,
					strength * 600,
					strength - 1
			);
			
			player.addEffect(poisonEffect);
		}
	}
}
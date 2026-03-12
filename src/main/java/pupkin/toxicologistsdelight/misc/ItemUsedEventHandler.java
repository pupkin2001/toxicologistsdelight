package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
		LivingEntity living = event.getEntity();
		
		ItemStack usedItem = event.getItem();
		
		if (!PoisonUtils.isPoisoned(usedItem)) return;
		
		int strength = PoisonUtils.getPoisonStrength(usedItem);
		
		if (usedItem.isEdible()) {
			living.addEffect(new MobEffectInstance(
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
			
			living.addEffect(poisonEffect);
		}
	}
	
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		ItemStack stack = event.getItemStack();
		if (!PoisonUtils.isPoisoned(stack)) return;
		
		if (!(event.getTarget() instanceof Animal animal)) return;
		
		if (animal.isFood(stack)) {
			int strength = PoisonUtils.getPoisonStrength(stack);
			animal.addEffect(new MobEffectInstance(
					MobEffects.POISON,
					strength * 600,
					strength - 1
			));
		}
	}
}
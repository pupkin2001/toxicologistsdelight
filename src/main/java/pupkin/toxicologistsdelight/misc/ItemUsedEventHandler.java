package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemUsedEventHandler
{
	@SubscribeEvent
	public static void onItemUsed(LivingEntityUseItemEvent.Finish event)
	{
		ItemStack used = event.getItem();
		if (used.isEdible() || used.getItem() instanceof PotionItem) { PoisonUtils.applyPoison(event.getEntity(), used); }
	}
	
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		if (event.getTarget() instanceof Animal animal && animal.isFood(event.getItemStack())) { PoisonUtils.applyPoison(animal, event.getItemStack()); }
	}
}

package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Animals fed by hand and mobs eating on their own are poisoned by the mixins
@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemUsedEventHandler
{
	@SubscribeEvent
	public static void onItemUsed(LivingEntityUseItemEvent.Finish event)
	{
		ItemStack used = event.getItem();
		if (PoisonUtils.isConsumable(used)) PoisonUtils.applyPoison(event.getEntity(), used);
	}
}

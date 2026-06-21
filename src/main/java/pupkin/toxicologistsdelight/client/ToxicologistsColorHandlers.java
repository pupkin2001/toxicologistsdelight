package pupkin.toxicologistsdelight.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.item.ColoredVial;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ToxicologistsColorHandlers {
	
	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		ItemColor vialColor = (stack, tintIndex) ->
				tintIndex > 0 ? -1
						: (stack.getItem() instanceof ColoredVial v ? v.getVialColor(stack) : -1);
		
		Item[] vials = ToxicologistsItems.ITEMS.getEntries().stream()
		                                       .map(RegistryObject::get)
		                                       .filter(i -> i instanceof ColoredVial)
		                                       .toArray(Item[]::new); // do NOT put ColoredVial[]::new regardless of how much the IDE wants it, that will throw ClassCastException
		
		if (vials.length > 0) event.register(vialColor, vials);
	}
}
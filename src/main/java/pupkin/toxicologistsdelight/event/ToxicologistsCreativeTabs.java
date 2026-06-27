package pupkin.toxicologistsdelight.event;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

@Mod.EventBusSubscriber(modid = "toxicologistsdelight", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ToxicologistsCreativeTabs
{
	
	@SubscribeEvent
	public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			event.accept(ToxicologistsItems.POISON_VIAL.get());
			event.accept(ToxicologistsItems.NECROTOXIN_VIAL.get());
			//event.accept(ToxicologistsItems.ROT_VIAL.get());
			//event.accept(ToxicologistsItems.HEMOTOXIN_VIAL.get());
			event.accept(ToxicologistsItems.ANTIDOTE_VIAL.get());
		} else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
			event.accept(ToxicologistsItems.EMPTY_VIAL.get());
		}
	}
}
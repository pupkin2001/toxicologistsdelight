package pupkin.toxicologistsdelight.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ToxicologistsItems
{
	public static final DeferredRegister<Item> ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, "toxicologistsdelight");
	public static final RegistryObject<Item> POISON_APPLICATOR = ITEMS.register(
			"poison_vial",
			PoisonApplicatorItem::new
	                                                                           );
	
	public static final RegistryObject<Item> EMPTY_VIAL = ITEMS.register(
			"empty_vial",
			() -> new Item(new Item.Properties())
	                                                                    );
	
	public static void register(IEventBus eventBus)
	{
		ITEMS.register(eventBus);
	}
}

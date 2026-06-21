package pupkin.toxicologistsdelight.item;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

public class ToxicologistsItems
{
	public static final DeferredRegister<Item> ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, "toxicologistsdelight");
	public static final RegistryObject<Item> EMPTY_VIAL = ITEMS.register(
			"empty_vial",
			() -> new Item(new Item.Properties())
	                                                                    );
	
	public static final RegistryObject<Item> ACID_VIAL = ITEMS.register("acid_vial",
	                                                                      () -> new EffectVialItem(new Item.Properties().stacksTo(16),
	                                                                                               ToxicologistsEffects.ACID, 1, 8));
	public static final RegistryObject<Item> POISON_VIAL = ITEMS.register("poison_vial",
	                                                                      () -> new EffectVialItem(new Item.Properties().stacksTo(16),
	                                                                                               () -> MobEffects.POISON, 1, 16));
	public static final RegistryObject<Item> ROT_VIAL = ITEMS.register("rot_vial",
	                                                                   () -> new EffectVialItem(new Item.Properties().stacksTo(16),
	                                                                                            ToxicologistsEffects.ROT, 1, 8));
	public static final RegistryObject<Item> REGEN_VIAL = ITEMS.register("regen_vial",
	                                                                     () -> new EffectVialItem(new Item.Properties().stacksTo(16),
	                                                                                              () -> MobEffects.REGENERATION, 1, 4));
	
	public static final RegistryObject<Item> CURE_VIAL = ITEMS.register(
			"cure_vial",
			PoisonCureItem::new
	                                                                    );
	
	public static void register(IEventBus eventBus)
	{
		ITEMS.register(eventBus);
	}
}

package pupkin.toxicologistsdelight.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

import java.util.function.Supplier;

public final class ToxicologistsItems
{
	
	public static final DeferredRegister<Item> ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, "toxicologistsdelight");
	
	public static final RegistryObject<Item>
			EMPTY_VIAL = ITEMS.register("empty_vial", () -> new Item(new Item.Properties())),
	
	ACID_VIAL = effectVial("acid_vial", ToxicologistsEffects.ACID, 1, 8),
			POISON_VIAL = effectVial("poison_vial", () -> MobEffects.POISON, 1, 16),
			ROT_VIAL = effectVial("rot_vial", ToxicologistsEffects.ROT, 1, 8),
			REGEN_VIAL = effectVial("regen_vial", () -> MobEffects.REGENERATION, 1, 4),
	
	CURE_VIAL = ITEMS.register("cure_vial", PoisonCureItem::new);
	
	private static RegistryObject<Item> effectVial(String name, Supplier<MobEffect> effect, int strength, int batchSize)
	{
		return ITEMS.register(name, () -> new EffectVialItem(
				new Item.Properties().stacksTo(16),
				effect, strength, batchSize));
	}
	
	public static void register(IEventBus eventBus)
	{
		ITEMS.register(eventBus);
	}
}

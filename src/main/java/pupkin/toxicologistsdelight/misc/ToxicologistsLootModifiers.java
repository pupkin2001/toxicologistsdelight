package pupkin.toxicologistsdelight.misc;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlockDropsModifier;

public final class ToxicologistsLootModifiers
{
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ToxicologistsDelight.MOD_ID);
	
	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> POISONED_BLOCK_DROPS =
			LOOT_MODIFIER_SERIALIZERS.register("poisoned_block_drops", PoisonedBlockDropsModifier.CODEC);
	
	private ToxicologistsLootModifiers() {}
	
	public static void register(IEventBus eventBus)
	{
		LOOT_MODIFIER_SERIALIZERS.register(eventBus);
	}
}

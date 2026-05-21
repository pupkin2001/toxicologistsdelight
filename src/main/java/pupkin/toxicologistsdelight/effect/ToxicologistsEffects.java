package pupkin.toxicologistsdelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

public class ToxicologistsEffects {
	public static final DeferredRegister<MobEffect> MOB_EFFECTS =
			DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ToxicologistsDelight.MOD_ID);
	
	public static final RegistryObject<MobEffect> ACID = MOB_EFFECTS.register("acid",
	                                                                                 () -> new AcidPoisonEffect(MobEffectCategory.HARMFUL, 0x48B518)); // greenish color
	
	public static void register(IEventBus eventBus) {
		MOB_EFFECTS.register(eventBus);
	}
}

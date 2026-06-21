package pupkin.toxicologistsdelight.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

public class ToxicologistsEffects
{
	public static final DeferredRegister<MobEffect> MOB_EFFECTS =
			DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ToxicologistsDelight.MOD_ID);
	
	public static final RegistryObject<MobEffect> ACID =
			MOB_EFFECTS.register("acid", AcidPoisonEffect::new);
	
	public static final RegistryObject<MobEffect> ROT =
			MOB_EFFECTS.register("rot", RotPoisonEffect::new);
	
	public static final RegistryObject<MobEffect> TOXICOSIS =
			MOB_EFFECTS.register("toxicosis", ToxicosisEffect::new);
	
	public static final RegistryObject<MobEffect> TOXIC_CRISIS =
			MOB_EFFECTS.register("toxic_crisis", ToxicosisEffect::new);
	
	public static void register(IEventBus eventBus)
	{
		MOB_EFFECTS.register(eventBus);
	}
}

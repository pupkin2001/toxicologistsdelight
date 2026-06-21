package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.RegistryObject;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import umpaz.brewinandchewin.common.fluid.AlcoholFluidType;

import java.awt.*;

public class ToxicologistsFluids
{
	public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, ToxicologistsDelight.MOD_ID);
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, ToxicologistsDelight.MOD_ID);
	
	public static final FluidRegistryObject ACID = registerAlcoholFluid("acid", new Color(98, 25, 109, 255)),
											CURE = registerAlcoholFluid("cure", new Color(255, 255, 255, 255));
	
	// Overly complex code duplicate from brewers delight. Go look there for comments on how this contraption works.
	private static FluidRegistryObject registerAlcoholFluid(String name, Color tintColor)
	{
		RegistryObject<FluidType> fluidType = FLUID_TYPES.register(name, () -> new AlcoholFluidType(tintColor.getRGB()));
		
		final RegistryObject<FlowingFluid>[] sourceHolder = new RegistryObject[1];
		final RegistryObject<FlowingFluid>[] flowingHolder = new RegistryObject[1];
		
		ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(
				fluidType,
				() -> sourceHolder[0].get(),
				() -> flowingHolder[0].get()
		);
		
		sourceHolder[0] = FLUIDS.register(name,
		                                  () -> new ForgeFlowingFluid.Source(properties));
		flowingHolder[0] = FLUIDS.register("flowing_" + name,
		                                   () -> new ForgeFlowingFluid.Flowing(properties));
		
		return new FluidRegistryObject(fluidType, sourceHolder[0], flowingHolder[0]);
	}
	
	private static ForgeFlowingFluid.Properties createFluidProperties(
			RegistryObject<FluidType> type,
			RegistryObject<FlowingFluid> source,
			RegistryObject<FlowingFluid> flowing)
	{
		return new ForgeFlowingFluid.Properties(type, source, flowing);
	}
	
	public static void register(IEventBus eventBus)
	{
		FLUID_TYPES.register(eventBus);
		FLUIDS.register(eventBus);
	}
	
	public record FluidRegistryObject(
			RegistryObject<FluidType> type,
			RegistryObject<FlowingFluid> source,
			RegistryObject<FlowingFluid> flowing
	) {}
}
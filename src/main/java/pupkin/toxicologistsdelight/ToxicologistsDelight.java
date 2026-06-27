package pupkin.toxicologistsdelight;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;
import pupkin.toxicologistsdelight.misc.ToxicologistsFluids;

@Mod(ToxicologistsDelight.MOD_ID)
public class ToxicologistsDelight
{
	public static final String MOD_ID = "toxicologistsdelight";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public ToxicologistsDelight(FMLJavaModLoadingContext context) {
		IEventBus eventBus = context.getModEventBus();
		
		ToxicologistsItems.register(eventBus);
		ToxicologistsEffects.register(eventBus);
		ToxicologistsFluids.register(eventBus);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
}
package pupkin.toxicologistsdelight;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

@Mod(ToxicologistsDelight.MOD_ID)
@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID)
public class ToxicologistsDelight
{
	public static final String MOD_ID = "toxicologistsdelight";
	
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public ToxicologistsDelight()
	{
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		ToxicologistsItems.register(eventBus);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
}

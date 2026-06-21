package pupkin.toxicologistsdelight.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler
{
	@SubscribeEvent
	public static void onItemTooltip(@NotNull ItemTooltipEvent event)
	{
		Player player = event.getEntity();
		if (player != null && !player.getAbilities().instabuild) return;
		if (PoisonUtils.isPoisoned(event.getItemStack())) {
			event.getToolTip().add(Component.literal("§cPoisoned!"));
		}
	}
}

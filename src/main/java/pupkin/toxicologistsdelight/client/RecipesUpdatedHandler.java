package pupkin.toxicologistsdelight.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.PoisonableItems;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class RecipesUpdatedHandler
{
	private RecipesUpdatedHandler() {}
	
	@SubscribeEvent
	public static void onRecipesUpdated(RecipesUpdatedEvent event)
	{
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) PoisonableItems.recompute(event.getRecipeManager(), connection.registryAccess());
	}
	
	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event)
	{
		if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection != null) PoisonableItems.recompute(connection.getRecipeManager(), connection.registryAccess());
	}
}

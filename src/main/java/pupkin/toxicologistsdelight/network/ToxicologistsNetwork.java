package pupkin.toxicologistsdelight.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import pupkin.toxicologistsdelight.ToxicologistsDelight;

public final class ToxicologistsNetwork
{
	private static final String PROTOCOL = "1";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
	
	private ToxicologistsNetwork() {}
	
	public static void register()
	{
		CHANNEL.messageBuilder(NecrosisIntensityPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
		       .encoder(NecrosisIntensityPacket::encode)
		       .decoder(NecrosisIntensityPacket::decode)
		       .consumerMainThread(NecrosisIntensityPacket::handle)
		       .add();
	}
	
	public static void sendTo(ServerPlayer player, Object packet)
	{
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}
}

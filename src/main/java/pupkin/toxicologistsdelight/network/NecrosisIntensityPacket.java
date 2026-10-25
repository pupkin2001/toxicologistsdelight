package pupkin.toxicologistsdelight.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import pupkin.toxicologistsdelight.client.NecroticHearts;

import java.util.function.Supplier;

/** How far Necrosis has ramped up (0 during the grace period, up to 1) */
public record NecrosisIntensityPacket(float intensity)
{
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeFloat(intensity);
	}
	
	public static NecrosisIntensityPacket decode(FriendlyByteBuf buf)
	{
		return new NecrosisIntensityPacket(buf.readFloat());
	}
	
	// Client bound only, and NecroticHearts touches no client-only classes, so this is safe to load anywhere.
	public void handle(Supplier<NetworkEvent.Context> context)
	{
		NecroticHearts.setIntensity(intensity);
	}
}

package pupkin.toxicologistsdelight.client;

import net.minecraft.world.entity.player.Player;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

import java.util.Random;

/** Per-heart jitter for the health bar while Necrosis is active, growing with its damage ramp via GuiMixin on the render thread. */
public final class NecroticHearts
{
	private static final Random RANDOM = new Random();
	private static final int THRASH_HEALTH = 8;
	
	/**
	 * Set from the server (NecrosisIntensityPacket) 0 during the grace period, up to 1 once the damage has fully ramped up
	 */
	private static float intensity;
	private static boolean active;
	private static boolean thrashing;
	private static long tick;
	private static int heart;
	private static int pendingYOffset;
	
	private NecroticHearts() {}
	
	public static void setIntensity(float value)
	{
		intensity = value;
	}
	
	public static void begin(Player player, int health)
	{
		if (!player.hasEffect(ToxicologistsEffects.NECROTOXIN.get())) intensity = 0.0F;
		active = intensity > 0.0F;
		thrashing = health <= THRASH_HEALTH;
		tick = player.level().getGameTime();
	}
	
	public static void setHeart(int index)
	{
		heart = index;
	}
	
	public static int shakeX(int x)
	{
		if (!active) return x;
		// Seeded by tick so each heart holds its offset for a tick, then jumps
		RANDOM.setSeed(tick * 31L + heart);
		int dx = RANDOM.nextInt(3) - 1;
		int dy = RANDOM.nextInt(5) - 2;
		if (thrashing) {
			dx += RANDOM.nextInt(5) - 1;
			dy += RANDOM.nextInt(7) - 3;
		}
		pendingYOffset = Math.round(dy * intensity);
		return x + Math.round(dx * intensity);
	}
	
	public static int shakeY(int y)
	{
		return active ? y + pendingYOffset : y;
	}
}

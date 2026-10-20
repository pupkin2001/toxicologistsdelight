package pupkin.toxicologistsdelight.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

import java.util.Random;

public class ToxicologistsClientEventHandler
{
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, value = Dist.CLIENT)
	public static class ClientEventHandler
	{
		private static final Random RANDOM = new Random();
		private static final ResourceLocation GUI_ICONS = ResourceLocation.withDefaultNamespace("textures/gui/icons.png");
		
		private static final int HEART_CONTAINER_U = 16;
		private static final int HEART_CONTAINER_BLINK_U = 25;
		private static final int HEART_FULL_U = 52;
		private static final int HEART_HALF_U = 61;
		private static final int HEART_FULL_BLINK_U = 70;
		private static final int HEART_HALF_BLINK_U = 79;
		private static final int HEART_ABSORB_FULL_U = 160;
		private static final int HEART_ABSORB_HALF_U = 169;
		private static final int HARDCORE_V = 45;
		private static final int NORMAL_V = 0;
		private static final ResourceLocation HEALTH_OVERLAY = VanillaGuiOverlay.PLAYER_HEALTH.id();
		
		// Damage blink state. ForgeGui keeps its own copy, but never updates it while we cancel its overlay.
		private static float lastHealth;
		private static int displayHealth;
		private static long lastHealthTime;
		private static long healthBlinkTime;
		
		@SubscribeEvent
		public static void onRenderHealth(RenderGuiOverlayEvent.Pre event)
		{
			if (!event.getOverlay().id().equals(HEALTH_OVERLAY)) return;
			
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			
			// Bail (WITHOUT cancelling) whenever vanilla itself wouldn't draw hearts.
			// canHurtPlayer() is false in creative AND spectator — that's your "renders
			// in creative" bug. hideGui covers F1. And because we don't set
			// receiveCanceled, if another health-bar mod already cancelled this overlay
			// we simply won't be called, so we won't fight it.
			if (player == null || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
			if (mc.options.hideGui) return;
			if (!player.hasEffect(ToxicologistsEffects.NECROTOXIN.get())) return;
			if (!(mc.gui instanceof ForgeGui gui)) return;
			
			// Only now are we deliberately taking over — stop vanilla drawing its own.
			event.setCanceled(true);
			
			int x = event.getWindow().getGuiScaledWidth() / 2 - 91;
			int y = event.getWindow().getGuiScaledHeight() - gui.leftHeight;
			boolean highlight = updateHealthBlink(player, gui.getGuiTicks());
			int rows = drawJumpingHearts(event.getGuiGraphics(), player, x, y, highlight);
			
			// Reserve the space we drew in, as ForgeGui#renderHealth would have, or the armour bar draws over the hearts.
			gui.leftHeight += rows * 10;
		}
		
		// Same bookkeeping as ForgeGui#renderHealth: blink for 20 ticks after taking damage (10 after healing
		// right after a hit), with the health from before the hit drawn as white hearts until it settles for a second.
		private static boolean updateHealthBlink(Player player, int guiTicks)
		{
			boolean highlight = healthBlinkTime > guiTicks && (healthBlinkTime - guiTicks) / 3L % 2L == 1L;
			float health = player.getHealth();
			long now = Util.getMillis();
			
			// Compared unrounded, unlike vanilla, so hits too small to change a half-heart (early in the ramp) still blink.
			if (health < lastHealth && player.invulnerableTime > 0) {
				lastHealthTime = now;
				healthBlinkTime = guiTicks + 20;
			} else if (health > lastHealth && player.invulnerableTime > 0) {
				lastHealthTime = now;
				healthBlinkTime = guiTicks + 10;
			}
			
			if (now - lastHealthTime > 1000L) {
				displayHealth = Mth.ceil(health);
				lastHealthTime = now;
			}
			
			lastHealth = health;
			return highlight;
		}
		
		/** Returns the number of 10-pixel heart rows drawn. */
		private static int drawJumpingHearts(GuiGraphics graphics, Player player, int baseX, int baseY, boolean highlight)
		{
			int health = Mth.ceil(player.getHealth());
			int absorption = Mth.ceil(player.getAbsorptionAmount());
			int maxHealth = Mth.ceil(player.getMaxHealth());
			boolean hardcore = player.level().getLevelData().isHardcore();
			long tick = player.level().getGameTime();
			int v = hardcore ? HARDCORE_V : NORMAL_V;
			
			int healthHearts = Mth.ceil(maxHealth / 2.0);   // empty containers + red fill
			int absorbHearts = Mth.ceil(absorption / 2.0);  // gold hearts, appended after
			int totalHearts = healthHearts + absorbHearts;
			
			for (int i = 0; i < totalHearts; i++) {
				int x = baseX + (i % 10) * 8;
				int y = baseY - (i / 10) * 10;
				
				// Per-heart jitter. Seeded by tick so it holds for a tick then
				// jumps; long math so tick * 31 doesn't overflow an int mid-multiply.
				RANDOM.setSeed(tick * 31L + i);
				x += RANDOM.nextInt(3) - 1;
				y += RANDOM.nextInt(5) - 2;
				if (health <= 8) {                  // extra thrashing at low health
					x += RANDOM.nextInt(5) - 1;
					y += RANDOM.nextInt(7) - 3;
				}
				
				drawHeart(graphics, x, y, highlight ? HEART_CONTAINER_BLINK_U : HEART_CONTAINER_U, v);
				if (i < healthHearts) {
					if (highlight) {
						int lost = displayHealth - i * 2;
						if (lost >= 2) { drawHeart(graphics, x, y, HEART_FULL_BLINK_U, v); } else if (lost == 1) drawHeart(graphics, x, y, HEART_HALF_BLINK_U, v);
					}
					int rem = health - i * 2;
					if (rem >= 2) { drawHeart(graphics, x, y, HEART_FULL_U, v); } else if (rem == 1) drawHeart(graphics, x, y, HEART_HALF_U, v);
				} else {
					int rem = absorption - (i - healthHearts) * 2;
					if (rem >= 2) { drawHeart(graphics, x, y, HEART_ABSORB_FULL_U, v); } else if (rem == 1) drawHeart(graphics, x, y, HEART_ABSORB_HALF_U, v);
				}
			}
			
			return Mth.ceil(totalHearts / 10.0);
		}
		
		// Draws a full 9x9 heart icon at the given UV
		private static void drawHeart(GuiGraphics graphics, int x, int y, int u, int v)
		{
			graphics.blit(GUI_ICONS, x, y, u, v, 9, 9);
		}
	}
}

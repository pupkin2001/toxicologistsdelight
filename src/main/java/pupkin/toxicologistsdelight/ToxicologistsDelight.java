package pupkin.toxicologistsdelight;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

import java.util.Random;

@Mod(ToxicologistsDelight.MOD_ID)
public class ToxicologistsDelight {
	public static final String MOD_ID = "toxicologistsdelight";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	public ToxicologistsDelight() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		ToxicologistsItems.register(eventBus);
		ToxicologistsEffects.register(eventBus);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, value = Dist.CLIENT)
	public static class ClientEventHandler {
		private static final Random RANDOM = new Random();
		private static final ResourceLocation GUI_ICONS = new ResourceLocation("textures/gui/icons.png");
		
		private static final int HEART_CONTAINER_U = 16;
		private static final int HEART_FULL_U = 52;
		private static final int HEART_HALF_U = 61;
		private static final int HEART_ABSORB_FULL_U = 160;
		private static final int HEART_ABSORB_HALF_U = 169;
		private static final int HARDCORE_V = 45;
		private static final int NORMAL_V = 0;
		private static final ResourceLocation HEALTH_OVERLAY = VanillaGuiOverlay.PLAYER_HEALTH.id();
		
		@SubscribeEvent
		public static void onRenderHealth(RenderGuiOverlayEvent.Pre event) {
			// Check if this is the health overlay
			if (!event.getOverlay().id().equals(HEALTH_OVERLAY)) return;
			
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || !mc.player.hasEffect(ToxicologistsEffects.ACID.get())) return;
			
			// Cancel vanilla hearts
			event.setCanceled(true);
			
			// Start in the same spot as vanilla (same layout)
			int screenWidth = event.getWindow().getGuiScaledWidth();
			int screenHeight = event.getWindow().getGuiScaledHeight();
			int x = screenWidth / 2 - 91;
			int y = screenHeight - 39;
			
			drawJumpingHearts(event.getGuiGraphics(), mc.player, x, y);
		}
		
		private static void drawJumpingHearts(GuiGraphics graphics, Player player, int baseX, int baseY) {
			int health = Mth.ceil(player.getHealth());
			int maxHealth = Mth.ceil(player.getMaxHealth());
			int absorption = Mth.ceil(player.getAbsorptionAmount());
			boolean hardcore = player.level().getLevelData().isHardcore();
			long tick = player.level().getGameTime();
			
			int maxHearts = Math.max(maxHealth / 2, 10);
			for (int i = 0; i < maxHearts; i++) {
				int x = baseX + (i % 10) * 8;
				
				int y = baseY - (i / 10) * 10;
				
				// Jumping animation per heart
				int seed = (int) (tick * 31 + i);
				RANDOM.setSeed(seed);
				int jumpX = RANDOM.nextInt(3) - 1;
				int jumpY = RANDOM.nextInt(5) - 2;
				if (player.getHealth() <= 8.0F) {
					jumpX += RANDOM.nextInt(5) - 1;
					jumpY += RANDOM.nextInt(7) - 3;
				}
				int heartX = x + jumpX;
				int heartY = y + jumpY;
				
				int healthRemaining = health - i * 2;
				int absorptionRemaining = absorption - i * 2;
				int v = hardcore ? HARDCORE_V : NORMAL_V;
				
				// Draw the container (empty) background
				drawHeart(graphics, heartX, heartY, HEART_CONTAINER_U, v);
				
				// Overlay the filling
				if (absorptionRemaining >= 2) {
					drawHeart(graphics, heartX, heartY, HEART_ABSORB_FULL_U, NORMAL_V);
				} else if (absorptionRemaining == 1) {
					drawHeart(graphics, heartX, heartY, HEART_ABSORB_HALF_U, NORMAL_V);
				} else if (healthRemaining >= 2) {
					drawHeart(graphics, heartX, heartY, HEART_FULL_U, v);
				} else if (healthRemaining == 1) {
					drawHeart(graphics, heartX, heartY, HEART_HALF_U, v);
				}
			}
		}
		
		// Draws a full 9x9 heart icon at the given UV
		private static void drawHeart(GuiGraphics graphics, int x, int y, int u, int v) {
			graphics.blit(GUI_ICONS, x, y, u, v, 9, 9);
		}
	}
}
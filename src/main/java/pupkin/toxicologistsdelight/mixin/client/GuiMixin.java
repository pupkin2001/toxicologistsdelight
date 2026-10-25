package pupkin.toxicologistsdelight.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.client.NecroticHearts;

// Shakes the vanilla hearts
@Mixin(Gui.class)
public abstract class GuiMixin
{
	private static final String RENDER_HEARTS = "renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V";
	
	@Inject(method = RENDER_HEARTS, at = @At("HEAD"))
	private void toxicologistsdelight$beginHearts(GuiGraphics graphics, Player player, int x, int y, int rowHeight, int regenHeart,
	                                              float maxHealth, int health, int displayHealth, int absorption, boolean highlight,
	                                              CallbackInfo ci)
	{
		NecroticHearts.begin(player, health);
	}
	
	// Int local #11 is the loop counter (the heart being drawn)
	@ModifyVariable(method = RENDER_HEARTS, at = @At("LOAD"), ordinal = 11)
	private int toxicologistsdelight$trackHeart(int heart)
	{
		NecroticHearts.setHeart(heart);
		return heart;
	}
	
	// Int local #14 is the heart's x position.
	@ModifyVariable(method = RENDER_HEARTS, at = @At(value = "STORE", ordinal = 0), ordinal = 14)
	private int toxicologistsdelight$shakeX(int x)
	{
		return NecroticHearts.shakeX(x);
	}
	
	// Int local #15 is the heart's y position
	@ModifyVariable(method = RENDER_HEARTS, at = @At(value = "STORE", ordinal = 0), ordinal = 15)
	private int toxicologistsdelight$shakeY(int y)
	{
		return NecroticHearts.shakeY(y);
	}
}

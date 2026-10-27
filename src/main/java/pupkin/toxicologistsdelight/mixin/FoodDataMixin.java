package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.effect.ToxicosisEffect;

// Returning right before either regeneration heal also skips the exhaustion
@Mixin(FoodData.class)
public abstract class FoodDataMixin
{
	@Inject(method = "tick(Lnet/minecraft/world/entity/player/Player;)V",
	        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"), cancellable = true)
	private void toxicologistsdelight$blockRegen(Player player, CallbackInfo ci)
	{
		if (ToxicosisEffect.blocksNaturalRegen(player)) ci.cancel();
	}
}

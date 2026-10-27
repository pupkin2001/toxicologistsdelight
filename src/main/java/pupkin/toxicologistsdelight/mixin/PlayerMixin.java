package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.effect.ToxicosisEffect;

// Peaceful difficulty regeneration is natural regeneration too
@Mixin(Player.class)
public abstract class PlayerMixin
{
	@Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
	private void toxicologistsdelight$beforePeacefulRegen(CallbackInfo ci)
	{
		ToxicosisEffect.NATURAL_REGEN.set(true);
	}
	
	@Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", shift = At.Shift.AFTER))
	private void toxicologistsdelight$afterPeacefulRegen(CallbackInfo ci)
	{
		ToxicosisEffect.NATURAL_REGEN.set(false);
	}
}

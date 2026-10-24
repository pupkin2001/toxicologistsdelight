package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Tamed wolves healed with meat shrink the stack themselves instead of calling usePlayerItem.
@Mixin(Wolf.class)
public abstract class WolfMixin
{
	@Inject(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
	        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;heal(F)V"))
	private void toxicologistsdelight$poisonOnHeal(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (PoisonUtils.isConsumable(stack)) PoisonUtils.applyPoison((Wolf) (Object) this, stack);
	}
}

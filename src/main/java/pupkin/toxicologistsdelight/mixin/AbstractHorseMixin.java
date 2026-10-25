package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Horses, donkeys, mules, llamas and camels eat through fedFood, which always consumes the item.
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin
{
	@Inject(method = "fedFood(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/InteractionResult;",
	        at = @At("HEAD"))
	private void toxicologistsdelight$poisonOnFeed(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (PoisonUtils.isConsumable(stack)) PoisonUtils.applyPoison((AbstractHorse) (Object) this, stack);
	}
}

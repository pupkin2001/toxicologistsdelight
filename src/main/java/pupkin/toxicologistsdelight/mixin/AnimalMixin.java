package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// usePlayerItem only runs once the animal actually eats what the player holds (breeding, taming, healing).
@Mixin(Animal.class)
public abstract class AnimalMixin
{
	@Inject(method = "usePlayerItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
	        at = @At("HEAD"))
	private void toxicologistsdelight$poisonOnFeed(Player player, InteractionHand hand, ItemStack stack, CallbackInfo ci)
	{
		if (PoisonUtils.isConsumable(stack)) PoisonUtils.applyPoison((Animal) (Object) this, stack);
	}
}

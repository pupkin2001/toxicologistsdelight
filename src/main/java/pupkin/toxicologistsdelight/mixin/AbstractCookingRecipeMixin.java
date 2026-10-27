package pupkin.toxicologistsdelight.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Smelting, smoking, blasting and campfire cooking (also Farmer's Delight's skillet)
@Mixin(AbstractCookingRecipe.class)
public abstract class AbstractCookingRecipeMixin
{
	@Inject(method = "assemble(Lnet/minecraft/world/Container;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$carryPoison(Container container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir)
	{
		// Only slot 0 is the ingredient - furnace slots 1 and 2 are its fuel and its previous output.
		ItemStack result = cir.getReturnValue();
		if (!result.isEmpty() && container.getContainerSize() > 0) PoisonUtils.propagate(Poison.of(container.getItem(0)), result);
	}
}

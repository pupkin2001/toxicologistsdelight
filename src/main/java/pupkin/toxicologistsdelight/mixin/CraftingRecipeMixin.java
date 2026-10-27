package pupkin.toxicologistsdelight.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Crafted result takes the strongest poison found
@Mixin({ShapedRecipe.class, ShapelessRecipe.class})
public abstract class CraftingRecipeMixin
{
	@Inject(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$carryPoison(CraftingContainer grid, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir)
	{
		PoisonUtils.propagate(Poison.strongestIn(grid), cir.getReturnValue());
	}
}

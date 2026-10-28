package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.brewinandchewin.KegPoison;
import umpaz.brewinandchewin.common.crafting.CreatePotionPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

// Create's potion fluid. This subclass builds the potion itself instead of calling KegPouringRecipe.assemble
// BnC only loads it when Create is installed
@Mixin(value = CreatePotionPouringRecipe.class, remap = false)
public abstract class CreatePotionPouringRecipeMixin
{
	@Inject(method = "assemble(Lumpaz/brewinandchewin/common/utility/KegRecipeWrapper;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$poisonPouredPotion(KegRecipeWrapper keg, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir)
	{
		KegPoison.pourInto((KegPouringRecipe) (Object) this, keg, cir.getReturnValue());
	}
}

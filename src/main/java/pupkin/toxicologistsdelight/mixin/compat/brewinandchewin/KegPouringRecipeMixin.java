package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.brewinandchewin.KegPoison;
import pupkin.toxicologistsdelight.utils.Poison;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

// Pouring between items and the keg
@Mixin(value = KegPouringRecipe.class, remap = false)
public abstract class KegPouringRecipeMixin
{
	// Item poured into the keg
	@Inject(method = "getFluid(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("RETURN"))
	private void toxicologistsdelight$poisonFluidFromItem(ItemStack stack, CallbackInfoReturnable<FluidStack> cir)
	{
		Poison poison = Poison.of(stack);
		if (poison != null) poison.applyTo(cir.getReturnValue());
	}
	
	// Keg fluid poured into an item
	@Inject(method = "assemble(Lumpaz/brewinandchewin/common/utility/KegRecipeWrapper;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$poisonPouredItem(KegRecipeWrapper keg, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir)
	{
		KegPoison.pourInto((KegPouringRecipe) (Object) this, keg, cir.getReturnValue());
	}
}

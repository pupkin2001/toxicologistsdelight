package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pupkin.toxicologistsdelight.utils.Poison;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

// A recipe's base fluid is untagged, so BnC's fluid comparison would stop a poisoned base fluid from fermenting
// It ferments like a clean one instead, and KegBlockEntityMixin carries its poison into the result
@Mixin(value = KegFermentingRecipe.class, remap = false)
public abstract class KegFermentingRecipeMixin
{
	@Redirect(method = "matches(Lumpaz/brewinandchewin/common/utility/KegRecipeWrapper;Lnet/minecraft/world/level/Level;)Z",
	          at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidStack;isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z"))
	private boolean toxicologistsdelight$ignorePoison(FluidStack tankFluid, FluidStack baseFluid)
	{
		return tankFluid.isFluidEqual(baseFluid) || (Poison.of(tankFluid) != null && Poison.fluidEqualIgnoringPoison(tankFluid, baseFluid));
	}
}

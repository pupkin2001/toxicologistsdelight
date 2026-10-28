package pupkin.toxicologistsdelight.compat.brewinandchewin;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

// Shared by the Brewin' and Chewin' keg mixins
public final class KegPoison
{
	private KegPoison() {}
	
	@Nullable
	public static Poison ofBatch(@NotNull KegFermentingRecipe recipe, @NotNull FluidTank tank, @NotNull IItemHandler inventory)
	{
		Poison base = recipe.getFluidIngredient() != null && !tank.isEmpty() ? Poison.of(tank.getFluid()) : null;
		return Poison.strongest(base, Poison.strongestIn(inventory, 0, KegFermentingRecipe.INPUT_SLOTS));
	}
	
	public static void pourInto(@NotNull KegPouringRecipe recipe, @NotNull KegRecipeWrapper keg, @NotNull ItemStack result)
	{
		FluidStack fluid = keg.getFluid(0);
		if (!fluid.isEmpty() && fluid.getFluid() == recipe.getRawFluid()) PoisonUtils.propagate(Poison.of(fluid), result);
	}
}

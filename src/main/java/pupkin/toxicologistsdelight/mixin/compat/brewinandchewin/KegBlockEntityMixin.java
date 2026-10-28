package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.brewinandchewin.KegPoison;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.function.Predicate;

// Fermentation makes new result stacks, so the batch's poison is read before and written onto them
@Mixin(value = KegBlockEntity.class, remap = false)
public abstract class KegBlockEntityMixin
{
	@Shadow
	@Final
	private ItemStackHandler inventory;
	@Shadow
	@Final
	private FluidTank fluidTank;
	@Shadow
	@Final
	private KegRecipeWrapper recipeWrapper;
	
	@Unique
	@Nullable
	private Poison toxicologistsdelight$batchPoison;
	
	@Inject(method = "processFermenting",
	        at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/common/crafting/KegFermentingRecipe;getResultFluid()Lnet/minecraft/world/level/material/Fluid;", ordinal = 0))
	private void toxicologistsdelight$readBatchPoison(KegFermentingRecipe recipe, KegBlockEntity keg, CallbackInfoReturnable<Boolean> cir)
	{
		toxicologistsdelight$batchPoison = KegPoison.ofBatch(recipe, fluidTank, inventory);
	}
	
	@ModifyArg(method = "processFermenting",
	           at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;setFluid(Lnet/minecraftforge/fluids/FluidStack;)V"))
	private FluidStack toxicologistsdelight$poisonFermentedFluid(FluidStack result)
	{
		if (toxicologistsdelight$batchPoison != null) toxicologistsdelight$batchPoison.applyTo(result);
		return result;
	}
	
	@ModifyArg(method = "processFermenting",
	           at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemStackHandler;insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;"),
	           index = 1)
	private ItemStack toxicologistsdelight$poisonFermentedItem(ItemStack result)
	{
		PoisonUtils.propagate(toxicologistsdelight$batchPoison, result);
		return result;
	}
	
	// BnC inserts an item result without checking the output slot and drops whatever doesn't fit, so with this batch waits until new result fits
	@Inject(method = "canFerment", at = @At("RETURN"), cancellable = true)
	private void toxicologistsdelight$waitForOutputSpace(KegFermentingRecipe recipe, KegBlockEntity keg, CallbackInfoReturnable<Boolean> cir)
	{
		Item resultItem = recipe.getResultItem();
		ItemStack output = inventory.getStackInSlot(KegBlockEntity.OUTPUT_SLOT);
		if (!cir.getReturnValueZ() || resultItem == null || output.isEmpty()) return;
		ItemStack result = new ItemStack(resultItem, recipe.getAmount());
		if (result.isEmpty()) return;
		
		// The same stack processFermenting would insert, poison included
		PoisonUtils.propagate(KegPoison.ofBatch(recipe, fluidTank, inventory), result);
		int limit = Math.min(inventory.getSlotLimit(KegBlockEntity.OUTPUT_SLOT), result.getMaxStackSize());
		if (!ItemHandlerHelper.canItemStacksStack(result, output) || output.getCount() + result.getCount() > limit) cir.setReturnValue(false);
	}
	
	@ModifyArg(method = "getMatchingRecipe",
	           at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
	private Predicate<KegFermentingRecipe> toxicologistsdelight$acceptPoisonedBase(Predicate<KegFermentingRecipe> original)
	{
		FluidStack tank = fluidTank.getFluid();
		if (tank.isEmpty() || Poison.of(tank) == null) return original;
		KegRecipeWrapper wrapper = recipeWrapper;
		Level level = ((BlockEntity) (Object) this).getLevel();
		return original.or(recipe -> recipe.getFluidIngredient() != null
				&& Poison.fluidEqualIgnoringPoison(tank, recipe.getFluidIngredient())
				&& recipe.matches(wrapper, level));
	}
}

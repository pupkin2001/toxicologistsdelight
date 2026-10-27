package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Objects;

// Cooked meal gets the strongest poison among the ingredients
// FD merges a new batch into the stored meal, and the stored meal into the output, by item
@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntityMixin
{
	@Shadow @Final private ItemStackHandler inventory;
	
	// Set when a batch completes and written onto the meal at the end of processCooking
	@Unique
	@Nullable
	private Poison toxicologistsdelight$batchPoison;
	
	// Batch carrying another poison waits until that one is served
	@Inject(method = "canCook(Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;)Z", at = @At("RETURN"), cancellable = true)
	private void toxicologistsdelight$keepMealsApart(CookingPotRecipe recipe, CallbackInfoReturnable<Boolean> cir)
	{
		ItemStack meal = inventory.getStackInSlot(CookingPotBlockEntity.MEAL_DISPLAY_SLOT);
		if (!cir.getReturnValueZ() || meal.isEmpty()) return;
		
		Poison batch = PoisonUtils.canBePoisoned(meal) ? Poison.strongestIn(inventory, 0, CookingPotBlockEntity.MEAL_DISPLAY_SLOT) : null;
		if (!Objects.equals(Poison.of(meal), batch)) cir.setReturnValue(false);
	}
	
	// Only reached once a batch is done, before the meal slot is written and before the ingredients are used
	@Inject(method = "processCooking(Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;Lvectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity;)Z",
	        at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getOutputContainer()Lnet/minecraft/world/item/ItemStack;"))
	private void toxicologistsdelight$readBatchPoison(CookingPotRecipe recipe, CookingPotBlockEntity cookingPot, CallbackInfoReturnable<Boolean> cir)
	{
		toxicologistsdelight$batchPoison = Poison.strongestIn(inventory, 0, CookingPotBlockEntity.MEAL_DISPLAY_SLOT);
	}
	
	@Inject(method = "processCooking(Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;Lvectorwing/farmersdelight/common/block/entity/CookingPotBlockEntity;)Z",
	        at = @At("RETURN"))
	private void toxicologistsdelight$poisonMeal(CookingPotRecipe recipe, CookingPotBlockEntity cookingPot, CallbackInfoReturnable<Boolean> cir)
	{
		// Either a fresh copy of the recipe result or the stored meal
		PoisonUtils.propagate(toxicologistsdelight$batchPoison, inventory.getStackInSlot(CookingPotBlockEntity.MEAL_DISPLAY_SLOT));
		toxicologistsdelight$batchPoison = null;
	}
	
	// The meal stays in its slot while the output holds the same meal with another poison
	@Inject(method = {"moveMealToOutput()V", "useStoredContainersOnMeal()V"}, at = @At("HEAD"), cancellable = true)
	private void toxicologistsdelight$keepOutputsApart(CallbackInfo ci)
	{
		ItemStack meal = inventory.getStackInSlot(CookingPotBlockEntity.MEAL_DISPLAY_SLOT);
		ItemStack output = inventory.getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT);
		if (!output.isEmpty() && output.getItem() == meal.getItem() && !Objects.equals(Poison.of(meal), Poison.of(output))) ci.cancel();
	}
}

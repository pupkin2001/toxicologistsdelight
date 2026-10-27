package pupkin.toxicologistsdelight.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.Poison;

import java.util.Objects;

// Vanilla stacks the new result onto the output slot by item
// A result with different poison now blocks the furnace like any other mismatched output; burn() checks canBurn first.
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin
{
	@SuppressWarnings("unchecked")
	@Inject(method = "canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z",
	        at = @At("RETURN"), cancellable = true)
	private void toxicologistsdelight$keepPoisonApart(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> items,
	                                                  int maxStackSize, CallbackInfoReturnable<Boolean> cir)
	{
		if (!cir.getReturnValueZ() || recipe == null) return;
		ItemStack output = items.get(2);
		if (output.isEmpty()) return;
		// Assemble again instead of predicting the poison
		ItemStack result = ((Recipe<Container>) recipe).assemble((Container) (Object) this, registryAccess);
		if (!Objects.equals(Poison.of(output), Poison.of(result))) cir.setReturnValue(false);
	}
}

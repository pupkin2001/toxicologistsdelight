package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.CuttingBoardContext;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.List;

// Rolled results are fresh copies. Outside a cutting board (CuttingBoardContext unset) they are left as is
@Mixin(value = CuttingBoardRecipe.class, remap = false)
public abstract class CuttingBoardRecipeMixin
{
	@Inject(method = "rollResults(Lnet/minecraft/util/RandomSource;I)Ljava/util/List;", at = @At("RETURN"))
	private void toxicologistsdelight$poisonResults(RandomSource random, int fortuneLevel, CallbackInfoReturnable<List<ItemStack>> cir)
	{
		Poison poison = CuttingBoardContext.current();
		if (poison == null) return;
		for (ItemStack result : cir.getReturnValue()) {
			PoisonUtils.propagate(poison, result);
		}
	}
}

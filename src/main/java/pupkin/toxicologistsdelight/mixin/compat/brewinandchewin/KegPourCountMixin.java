package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;

import java.util.Optional;

// Brewin' and Chewin' 3.2 sizes pouring by the keg's capacity instead of what is in it
// https://github.com/Monad-Modding/BrewinAndChewin/issues/42

// These put back 3.1.2's limits. Only 3.2's signature of fluidExtract is targeted (require = 0)
@Mixin(value = KegBlockEntity.class, remap = false)
public abstract class KegPourCountMixin
{
	private static final String FLUID_EXTRACT = "fluidExtract(Lnet/minecraft/world/item/ItemStack;IZZ)Ljava/util/List;";
	
	@Shadow
	@Final
	private FluidTank fluidTank;
	
	@Shadow
	public abstract Optional<KegPouringRecipe> getPouringRecipe(ItemStack slot);
	
	@Redirect(method = FLUID_EXTRACT, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I", ordinal = 1, remap = true), require = 0)
	private int toxicologistsdelight$fillCount(int value, int min, int max, ItemStack slotIn, int maxTakeAmount)
	{
		int perContainer = toxicologistsdelight$perContainer(slotIn);
		if (perContainer <= 0) return Mth.clamp(value, min, max);
		return Mth.clamp(Math.min(slotIn.getCount(), (fluidTank.getCapacity() - fluidTank.getFluidAmount()) / perContainer), 1, maxTakeAmount);
	}
	
	@Redirect(method = FLUID_EXTRACT, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I", ordinal = 0, remap = true), require = 0)
	private int toxicologistsdelight$drainCount(int value, int min, int max, ItemStack slotIn, int maxTakeAmount)
	{
		int perContainer = toxicologistsdelight$perContainer(slotIn);
		if (perContainer <= 0) return Mth.clamp(value, min, max);
		return Mth.clamp(Math.min(slotIn.getCount(), fluidTank.getFluidAmount() / perContainer), 1, maxTakeAmount);
	}
	
	@Unique
	private int toxicologistsdelight$perContainer(ItemStack slotIn)
	{
		return getPouringRecipe(slotIn).map(KegPouringRecipe::getAmount).orElse(0);
	}
}

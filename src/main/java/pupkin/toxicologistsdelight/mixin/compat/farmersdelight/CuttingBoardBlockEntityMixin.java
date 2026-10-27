package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.CuttingBoardContext;
import pupkin.toxicologistsdelight.utils.Poison;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

// Exposes the poison of the item being cut while the board processes it; CuttingBoardRecipeMixin puts it on the results.
@Mixin(value = CuttingBoardBlockEntity.class, remap = false)
public abstract class CuttingBoardBlockEntityMixin
{
	@Shadow
	public abstract ItemStack getStoredItem();
	
	@Inject(method = "processStoredItemUsingTool(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"))
	private void toxicologistsdelight$beginCutting(ItemStack toolStack, @Nullable Player player, CallbackInfoReturnable<Boolean> cir)
	{
		CuttingBoardContext.begin(Poison.of(getStoredItem()));
	}
	
	@Inject(method = "processStoredItemUsingTool(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"))
	private void toxicologistsdelight$endCutting(ItemStack toolStack, @Nullable Player player, CallbackInfoReturnable<Boolean> cir)
	{
		CuttingBoardContext.end();
	}
}

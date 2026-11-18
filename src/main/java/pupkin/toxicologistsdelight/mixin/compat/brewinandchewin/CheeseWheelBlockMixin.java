package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlocks;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import umpaz.brewinandchewin.common.block.CheeseWheelBlock;

// A placed cheese wheel keeps its poison in PoisonedBlocks; every wedge cut from it carries that poison.
// use runs on both sides; only the server has the data, and only the server pops the wedge. use overrides a vanilla method, so its name is remapped.
@Mixin(value = CheeseWheelBlock.class, remap = false)
public abstract class CheeseWheelBlockMixin
{
	@Unique
	private static final String toxicologistsdelight$USE = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;";
	
	// The wedge is popped before the servings go down, or the last one removes the wheel (without drops)
	@Redirect(method = toxicologistsdelight$USE, remap = true,
	          at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/common/block/CheeseWheelBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", remap = true))
	private void toxicologistsdelight$poisonWedge(Level level, BlockPos pos, ItemStack wedge, BlockState state)
	{
		if (level instanceof ServerLevel serverLevel) PoisonUtils.propagate(PoisonedBlocks.get(serverLevel).poisonAt(pos, state.getBlock()), wedge);
		Block.popResource(level, pos, wedge);
	}
	
	@Inject(method = toxicologistsdelight$USE, remap = true, at = @At("RETURN"))
	private void toxicologistsdelight$forgetEatenWheel(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult,
	                                                   CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonedBlocks.get(serverLevel).forgetIfGone(serverLevel, pos);
	}
}

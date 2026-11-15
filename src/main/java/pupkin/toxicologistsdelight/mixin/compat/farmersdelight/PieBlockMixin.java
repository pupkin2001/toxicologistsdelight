package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlocks;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.block.PieBlock;

// A placed pie keeps its poison in PoisonedBlocks; every slice eaten or cut from it carries that poison.
// Both methods run on both sides; only the server has the data.
@Mixin(value = PieBlock.class, remap = false)
public abstract class PieBlockMixin
{
	// The first ItemStack local is the bite's slice. FD copies it into LivingEntityUseItemEvent.Finish, where ItemUsedEventHandler poisons the eater.
	@ModifyVariable(method = "consumeBite(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;",
	                at = @At("STORE"), ordinal = 0)
	private ItemStack toxicologistsdelight$poisonBite(ItemStack slice, Level level, BlockPos pos, BlockState state)
	{
		return toxicologistsdelight$poison(slice, level, pos, state);
	}
	
	// FD removes the pie before making its last slice. poisonAt doesn't look at the block, and the entry is only dropped on return.
	@Redirect(method = "cutSlice(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;",
	          at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/block/PieBlock;getPieSliceItem()Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack toxicologistsdelight$poisonCutSlice(PieBlock pie, Level level, BlockPos pos, BlockState state)
	{
		return toxicologistsdelight$poison(pie.getPieSliceItem(), level, pos, state);
	}
	
	@Inject(method = {
			"consumeBite(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;",
			"cutSlice(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;"
	}, at = @At("RETURN"))
	private void toxicologistsdelight$forgetEatenPie(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonedBlocks.get(serverLevel).forgetIfGone(serverLevel, pos);
	}
	
	@Unique
	private ItemStack toxicologistsdelight$poison(ItemStack slice, Level level, BlockPos pos, BlockState state)
	{
		if (level instanceof ServerLevel serverLevel) PoisonUtils.propagate(PoisonedBlocks.get(serverLevel).poisonAt(pos, state.getBlock()), slice);
		return slice;
	}
}

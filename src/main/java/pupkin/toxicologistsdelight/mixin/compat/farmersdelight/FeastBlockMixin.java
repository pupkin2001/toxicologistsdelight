package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlocks;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.block.FeastBlock;

// A placed feast keeps its poison in PoisonedBlocks; every serving taken from it carries that poison.
// takeServing runs on both sides; only the server's serving is real, and only the server has the data.
@Mixin(value = FeastBlock.class, remap = false)
public abstract class FeastBlockMixin
{
	private static final String TAKE_SERVING = "takeServing(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;";
	
	// The first ItemStack local is the serving, made before the servings go down or the block is removed.
	@ModifyVariable(method = TAKE_SERVING, at = @At("STORE"), ordinal = 0)
	private ItemStack toxicologistsdelight$poisonServing(ItemStack serving, LevelAccessor level, BlockPos pos, BlockState state)
	{
		if (level instanceof ServerLevel serverLevel) PoisonUtils.propagate(PoisonedBlocks.get(serverLevel).poisonAt(pos, state.getBlock()), serving);
		return serving;
	}
	
	// The client can't tell a poisoned block from a clean one, so the clean serving it predicted creates a ghost item. Just ignore prediction and let slot updates do their thing
	@Redirect(method = TAKE_SERVING,
	          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z", remap = true))
	private boolean toxicologistsdelight$leaveServingToServer(Inventory inventory, ItemStack serving, LevelAccessor level)
	{
		return level.isClientSide() || inventory.add(serving);
	}
	
	// The last serving may remove the feast; with no servings left the feast is broken (its drops go through PoisonedBlockDropsModifier first).
	@Inject(method = TAKE_SERVING, at = @At("RETURN"))
	private void toxicologistsdelight$forgetEatenFeast(LevelAccessor level, BlockPos pos, BlockState state, Player player, InteractionHand hand,
	                                                   CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonedBlocks.get(serverLevel).forgetIfGone(serverLevel, pos);
	}
}

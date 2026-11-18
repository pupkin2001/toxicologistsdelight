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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlocks;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import umpaz.brewinandchewin.common.block.PizzaBlock;

import java.util.Objects;

// A placed pizza keeps its poison in PoisonedBlocks; every slice taken from it carries that poison.
// A slice goes into an empty hand, onto the slices already in hand, or is popped when the hand holds anything else; all before the servings go down
// or the last one removes the pizza (without drops). takeServing runs on both sides; only the server has the data.
@Mixin(value = PizzaBlock.class, remap = false)
public abstract class PizzaBlockMixin
{
	private static final String TAKE_SERVING = "takeServing(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;";
	
	@Redirect(method = TAKE_SERVING,
	          at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V", remap = true))
	private void toxicologistsdelight$poisonSliceInHand(Player player, InteractionHand hand, ItemStack slice, Level level, BlockPos pos, BlockState state)
	{
		PoisonUtils.propagate(toxicologistsdelight$poisonAt(level, pos, state), slice);
		player.setItemInHand(hand, slice);
	}
	
	// Captured arguments start from takeServing's first, so its level and position come again before the state
	@Redirect(method = TAKE_SERVING,
	          at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/common/block/PizzaBlock;popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", remap = true))
	private void toxicologistsdelight$poisonPoppedSlice(Level level, BlockPos pos, ItemStack slice, Level pizzaLevel, BlockPos pizzaPos, BlockState state)
	{
		PoisonUtils.propagate(toxicologistsdelight$poisonAt(level, pos, state), slice);
		Block.popResource(level, pos, slice);
	}
	
	// Skip client prediction to avoid ghost items
	@Redirect(method = TAKE_SERVING, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setCount(I)V", remap = true))
	private void toxicologistsdelight$addSliceInHand(ItemStack held, int count, Level level, BlockPos pos, BlockState state)
	{
		if (level.isClientSide()) return;
		Poison poison = toxicologistsdelight$poisonAt(level, pos, state);
		if (Objects.equals(poison, Poison.of(held))) {
			held.setCount(count);
			return;
		}
		ItemStack slice = new ItemStack(held.getItem());
		PoisonUtils.propagate(poison, slice);
		Block.popResource(level, pos, slice);
	}
	
	@Inject(method = TAKE_SERVING, at = @At("RETURN"))
	private void toxicologistsdelight$forgetEatenPizza(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonedBlocks.get(serverLevel).forgetIfGone(serverLevel, pos);
	}
	
	@Unique
	@Nullable
	private static Poison toxicologistsdelight$poisonAt(Level level, BlockPos pos, BlockState state)
	{
		return level instanceof ServerLevel serverLevel ? PoisonedBlocks.get(serverLevel).poisonAt(pos, state.getBlock()) : null;
	}
}

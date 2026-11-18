package pupkin.toxicologistsdelight.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlocks;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// A placed cake keeps its poison in PoisonedBlocks; every bite carries that poison. Candle cakes pass a plain cake state.
@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin
{
	@Unique
	private static final String toxicologistsdelight$EAT = "eat(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/InteractionResult;";
	
	// Only reached once the player can eat; the poison is read before changing the cake
	@Inject(method = toxicologistsdelight$EAT, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
	private static void toxicologistsdelight$poisonBite(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonUtils.applyPoison(player, PoisonedBlocks.get(serverLevel).poisonAt(pos, state.getBlock()));
	}
	
	// The last bite removes the cake
	@Inject(method = toxicologistsdelight$EAT, at = @At("RETURN"))
	private static void toxicologistsdelight$forgetEatenCake(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (level instanceof ServerLevel serverLevel) PoisonedBlocks.get(serverLevel).forgetIfGone(serverLevel, pos);
	}
}

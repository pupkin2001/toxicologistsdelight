package pupkin.toxicologistsdelight.compat.farmersdelight;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PoisonedBlockEvents
{
	private PoisonedBlockEvents() {}
	
	// Lowest priority to ignore events cancelled by other mods
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event)
	{
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		// Entities other than players Forge reports the replaced block in getPlacedBlock()
		Block block = level.getBlockState(event.getPos()).getBlock();
		if (!PoisonUtils.isPoisonableBlock(block)) return;
		PoisonedBlocks.get(level).setPoison(event.getPos(), block, Poison.of(placedFrom(event.getEntity(), block)));
	}
	
	// Creative mode breaks without loot, so PoisonedBlockDropsModifier never gets them
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBroken(BlockEvent.BreakEvent event)
	{
		if (event.getLevel() instanceof ServerLevel level && PoisonUtils.isPoisonableBlock(event.getState().getBlock())) {
			PoisonedBlocks.get(level).forgetLaterIfGone(event.getPos());
		}
	}
	
	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) PoisonedBlocks.get(level).runPendingChecks(level);
	}
	
	private static ItemStack placedFrom(@Nullable Entity entity, Block block)
	{
		if (!(entity instanceof LivingEntity living)) return ItemStack.EMPTY;
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = living.getItemInHand(hand);
			if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) return stack;
		}
		return ItemStack.EMPTY;
	}
}

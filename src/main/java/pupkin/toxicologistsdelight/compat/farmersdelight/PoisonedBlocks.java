package pupkin.toxicologistsdelight.compat.farmersdelight;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.Objects;

/**
 * Poison placed food blocks ({@link PoisonUtils#isPoisonableBlock}), which have no block entity to hold it. One instance per {@link ServerLevel}. Each entry remembers the block
 * it was placed as, so a different block later put at the same position doesn't inherit it. The same food in another form keeps it ({@link PoisonUtils#isSameFood}).
 */
public final class PoisonedBlocks extends SavedData
{
	private static final String NAME = ToxicologistsDelight.MOD_ID + "_poisoned_blocks";
	private static final String ENTRIES_KEY = "Entries";
	private static final String POS_KEY = "Pos";
	private static final String BLOCK_KEY = "Block";
	
	private final Long2ObjectMap<Placed> entries = new Long2ObjectOpenHashMap<>();
	
	private final LongSet pendingChecks = new LongOpenHashSet();
	
	public static PoisonedBlocks get(@NotNull ServerLevel level)
	{
		return level.getDataStorage().computeIfAbsent(PoisonedBlocks::load, PoisonedBlocks::new, NAME);
	}
	
	private static PoisonedBlocks load(CompoundTag tag)
	{
		PoisonedBlocks data = new PoisonedBlocks();
		ListTag list = tag.getList(ENTRIES_KEY, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			Poison poison = Poison.of(entry);
			ResourceLocation blockId = ResourceLocation.tryParse(entry.getString(BLOCK_KEY));
			// Unknown ids (blocks from removed mods) are dropped. Not isPoisonableBlock as poisonable items may not be derived yet
			Block block = blockId == null || !ForgeRegistries.BLOCKS.containsKey(blockId) ? null : ForgeRegistries.BLOCKS.getValue(blockId);
			if (poison != null && block != null && PoisonUtils.isPoisonableBlock(block)) data.entries.put(entry.getLong(POS_KEY), new Placed(poison, block));
		}
		return data;
	}
	
	/** The poison recorded at {@code pos} for {@code block} or the same food in another form */
	@Nullable
	public Poison poisonAt(@NotNull BlockPos pos, @NotNull Block block)
	{
		Placed placed = entries.get(pos.asLong());
		return placed != null && PoisonUtils.isSameFood(placed.block(), block) ? placed.poison() : null;	}
	
	/** Records {@code poison} for {@code block} at {@code pos}; {@code null} clears the position. */
	public void setPoison(@NotNull BlockPos pos, @NotNull Block block, @Nullable Poison poison)
	{
		Placed placed = poison == null ? null : new Placed(poison, block);
		Placed previous = placed == null ? entries.remove(pos.asLong()) : entries.put(pos.asLong(), placed);
		if (!Objects.equals(previous, placed)) setDirty();
	}
	
	/** Forgets the entry at {@code pos} once its food is no longer there. */
	public void forgetIfGone(@NotNull ServerLevel level, @NotNull BlockPos pos)
	{
		Placed placed = entries.get(pos.asLong());
		if (placed == null || !level.isLoaded(pos) || PoisonUtils.isSameFood(placed.block(), level.getBlockState(pos).getBlock())) return;
		entries.remove(pos.asLong());
		setDirty();
	}
	
	/**
	 * Like {@link #forgetIfGone}, but at the end of the level tick.
	 */
	// a broken block is already air by the time loot is generated, so this is needed
	public void forgetLaterIfGone(@NotNull BlockPos pos)
	{
		if (entries.containsKey(pos.asLong())) pendingChecks.add(pos.asLong());
	}
	
	public void runPendingChecks(@NotNull ServerLevel level)
	{
		if (pendingChecks.isEmpty()) return;
		LongIterator positions = pendingChecks.iterator();
		while (positions.hasNext()) {
			forgetIfGone(level, BlockPos.of(positions.nextLong()));
		}
		pendingChecks.clear();
	}
	
	@Override
	@NotNull
	public CompoundTag save(@NotNull CompoundTag tag)
	{
		ListTag list = new ListTag();
		for (Long2ObjectMap.Entry<Placed> e : entries.long2ObjectEntrySet()) {
			ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(e.getValue().block());
			if (blockId == null) continue;
			CompoundTag entry = e.getValue().poison().toTag();
			entry.putLong(POS_KEY, e.getLongKey());
			entry.putString(BLOCK_KEY, blockId.toString());
			list.add(entry);
		}
		tag.put(ENTRIES_KEY, list);
		return tag;
	}
	
	private record Placed(Poison poison, Block block) {}
}

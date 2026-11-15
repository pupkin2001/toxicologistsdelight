package pupkin.toxicologistsdelight.compat.farmersdelight;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * Poisons what a poisoned food block drops Farmer's Delight's slicing modifier is covered by PastrySlicingModifierMixin
 */
public class PoisonedBlockDropsModifier extends LootModifier
{
	public static final Supplier<Codec<PoisonedBlockDropsModifier>> CODEC = Suppliers.memoize(
			() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, PoisonedBlockDropsModifier::new)));
	
	public PoisonedBlockDropsModifier(LootItemCondition[] conditions)
	{
		super(conditions);
	}
	
	public static void poisonDrops(@NotNull List<ItemStack> drops, @NotNull LootContext context)
	{
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
		if (state == null || origin == null || !PoisonUtils.isPoisonableBlock(state.getBlock())) return;
		
		BlockPos pos = BlockPos.containing(origin);
		PoisonedBlocks poisonedBlocks = PoisonedBlocks.get(context.getLevel());
		Poison poison = poisonedBlocks.poisonAt(pos, state.getBlock());
		if (poison == null) return;
		for (ItemStack drop : drops) {
			PoisonUtils.propagate(poison, drop);
		}
		poisonedBlocks.forgetLaterIfGone(pos);
	}
	
	@Override
	@NotNull
	protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context)
	{
		poisonDrops(generatedLoot, context);
		return generatedLoot;
	}
	
	@Override
	public Codec<? extends IGlobalLootModifier> codec()
	{
		return CODEC.get();
	}
}

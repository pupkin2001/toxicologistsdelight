package pupkin.toxicologistsdelight.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

public class PoisonAntidoteItem extends AbstractVialItem
{
	private static final int BATCH_SIZE = 8;
	private static final int DRINK_DURATION = 32;
	
	public PoisonAntidoteItem()
	{
		super(new Properties().stacksTo(16));
	}
	
	@Override
	public int getVialColor(ItemStack stack)
	{
		return 0xFFFFFF;
	}
	
	@Override
	protected boolean canTransform(ItemStack target)
	{
		return PoisonUtils.isPoisoned(target);
	}
	
	@Override
	protected int batchSize()
	{
		return BATCH_SIZE;
	}
	
	@Override
	protected ItemStack transform(ItemStack base)
	{
		return PoisonUtils.cleanseItem(base);
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand)
	{
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		if (player.getItemInHand(otherHand).isEmpty()) {
			if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.pass(player.getItemInHand(hand));
			return ItemUtils.startUsingInstantly(level, player, hand);
		}
		return super.use(level, player, hand);
	}
	
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
	{
		return UseAnim.DRINK;
	}
	
	@Override
	public int getUseDuration(@NotNull ItemStack stack)
	{
		return DRINK_DURATION;
	}
	
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity)
	{
		if (!level.isClientSide()) {
			entity.curePotionEffects(stack);
			entity.removeEffect(MobEffects.POISON);
		}
		if (entity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}
		entity.gameEvent(GameEvent.DRINK);
		
		if (entity instanceof Player player && player.getAbilities().instabuild) return stack;
		
		stack.shrink(1);
		ItemStack emptyVial = new ItemStack(ToxicologistsItems.EMPTY_VIAL.get());
		if (stack.isEmpty()) return emptyVial;
		if (entity instanceof Player player) giveOrDrop(player, emptyVial);
		return stack;
	}
}

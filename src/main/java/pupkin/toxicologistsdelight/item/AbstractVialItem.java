package pupkin.toxicologistsdelight.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractVialItem extends Item implements ColoredVial
{
	
	private static final int TARGET_COOLDOWN_TICKS = 20;
	private static final int VIAL_COOLDOWN_TICKS = 10;
	
	protected AbstractVialItem(Properties properties)
	{
		super(properties);
	}
	
	private static boolean isConsumable(ItemStack stack)
	{
		return stack.isEdible() || stack.getItem() instanceof PotionItem;
	}
	
	private static void giveOrDrop(Player player, ItemStack stack)
	{
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
	
	/** Extra condition the off-hand target must meet (e.g. poisoned / not poisoned). */
	protected abstract boolean canTransform(ItemStack target);
	
	/** Maximum number of items from one stack a single use modifies. */
	protected abstract int batchSize();
	
	/** Transforms {@code base} (already sized to the batch) into the result stack. */
	protected abstract ItemStack transform(ItemStack base);
	
	/**
	 * Sound played server-side when the vial is successfully used.
	 */
	protected SoundEvent useSound()
	{
		return SoundEvents.BOTTLE_EMPTY;
	}
	
	/** Volume of {@link #useSound()}. */
	protected float useSoundVolume()
	{
		return 1.0F;
	}
	
	/** Pitch of {@link #useSound()}. */
	protected float useSoundPitch()
	{
		return 1.0F;
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand)
	{
		ItemStack vial = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.pass(vial);
		}
		
		ItemStack target = player.getOffhandItem();
		if (player.getCooldowns().isOnCooldown(vial.getItem())
				|| player.getCooldowns().isOnCooldown(target.getItem())) {
			return InteractionResultHolder.pass(vial);
		}
		if (!isConsumable(target) || !canTransform(target)) {
			return InteractionResultHolder.pass(vial);
		}
		
		player.getCooldowns().addCooldown(target.getItem(), TARGET_COOLDOWN_TICKS);
		player.getCooldowns().addCooldown(this, VIAL_COOLDOWN_TICKS);
		
		if (!level.isClientSide()) {
			transformOffhand(player, target);
			consumeVial(player, vial);
			playUseSound(level, player);
		}
		return InteractionResultHolder.sidedSuccess(vial, level.isClientSide());
	}
	
	private void playUseSound(Level level, Player player)
	{
		level.playSound(
				null, // null -> server broadcasts to all nearby players, including the user
				player.getX(), player.getY(), player.getZ(),
				useSound(),
				SoundSource.PLAYERS,
				useSoundVolume(),
				useSoundPitch());
	}
	
	private void transformOffhand(Player player, ItemStack target)
	{
		int batch = Math.min(batchSize(), target.getCount());
		int remainder = target.getCount() - batch;
		
		ItemStack base = target.copy();
		base.setCount(batch);
		ItemStack result = transform(base);
		
		if (remainder > 0) {
			ItemStack keep = target.copy();
			keep.setCount(remainder);
			player.setItemInHand(InteractionHand.OFF_HAND, keep);
			giveOrDrop(player, result);
		} else {
			player.setItemInHand(InteractionHand.OFF_HAND, result);
		}
	}
	
	private void consumeVial(Player player, ItemStack vial)
	{
		if (player.getAbilities().instabuild) {
			return;
		}
		vial.shrink(1);
		giveOrDrop(player, new ItemStack(ToxicologistsItems.EMPTY_VIAL.get()));
	}
}

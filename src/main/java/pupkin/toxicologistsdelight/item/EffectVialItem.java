package pupkin.toxicologistsdelight.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.function.Supplier;

public class EffectVialItem extends Item implements ColoredVial
{
	private final Supplier<MobEffect> effect; // Supplier so the effect can register after the item
	private final int strength;
	private final int cap;
	
	public EffectVialItem(Properties props, Supplier<MobEffect> effect, int strength, int cap) {
		super(props);
		this.effect = effect; this.strength = strength; this.cap = cap;
	}
	
	@Override
	public int getVialColor(ItemStack stack) {
		return effect.get().getColor();
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack vial = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(vial);
		
		ItemStack target = player.getOffhandItem();
		if (player.getCooldowns().isOnCooldown(vial.getItem()) || player.getCooldowns().isOnCooldown(target.getItem())) return InteractionResultHolder.pass(vial);
		boolean applicable = target.isEdible() || target.getItem() instanceof PotionItem;
		if (!applicable || PoisonUtils.isPoisoned(target)) return InteractionResultHolder.pass(vial);
		
		player.getCooldowns().addCooldown(target.getItem(), 20);
		player.getCooldowns().addCooldown(this, 10);
		
		if (!level.isClientSide()) {
			int have      = target.getCount();
			int toPoison  = Math.min(cap, have);
			int remainder = have - toPoison;
			
			ItemStack base = target.copy();
			base.setCount(toPoison);
			ItemStack poisoned = PoisonUtils.poisonItem(base, effect.get(), strength);
			
			if (remainder > 0) {
				ItemStack keep = target.copy();
				keep.setCount(remainder);
				player.setItemInHand(InteractionHand.OFF_HAND, keep);
				if (!player.getInventory().add(poisoned)) player.drop(poisoned, false);
			} else {
				player.setItemInHand(InteractionHand.OFF_HAND, poisoned);
			}
			
			if (!player.getAbilities().instabuild) {
				vial.shrink(1);
				ItemStack empty = new ItemStack(ToxicologistsItems.EMPTY_VIAL.get());
				if (!player.getInventory().add(empty)) player.drop(empty, false);
			}
		}
		return InteractionResultHolder.sidedSuccess(vial, level.isClientSide());
	}
}

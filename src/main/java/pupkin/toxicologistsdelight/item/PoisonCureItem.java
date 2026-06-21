package pupkin.toxicologistsdelight.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

public class PoisonCureItem extends Item implements ColoredVial
{
	public PoisonCureItem()
	{
		super(new Properties()
				      .stacksTo(16)
		     );
	}
	
	public int getVialColor(ItemStack stack) {
		return 0xFFFFFF;
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack vial = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(vial);
		
		ItemStack target = player.getOffhandItem();
		if (player.getCooldowns().isOnCooldown(vial.getItem()) || player.getCooldowns().isOnCooldown(target.getItem())) return InteractionResultHolder.pass(vial);
		boolean applicable = target.isEdible() || target.getItem() instanceof PotionItem;
		if (!applicable || !PoisonUtils.isPoisoned(target)) return InteractionResultHolder.pass(vial);
		
		player.getCooldowns().addCooldown(target.getItem(), 20);
		player.getCooldowns().addCooldown(this, 10);
		
		if (!level.isClientSide()) {
			int have      = target.getCount();
			int toCleanse  = Math.min(8, have);
			int remainder = have - toCleanse;
			
			ItemStack base = target.copy();
			base.setCount(toCleanse);
			ItemStack cleansed = PoisonUtils.cleanseItem(base);
			
			if (remainder > 0) {
				ItemStack keep = target.copy();
				keep.setCount(remainder);
				player.setItemInHand(InteractionHand.OFF_HAND, keep);
				if (!player.getInventory().add(cleansed)) player.drop(cleansed, false);
			} else {
				player.setItemInHand(InteractionHand.OFF_HAND, cleansed);
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

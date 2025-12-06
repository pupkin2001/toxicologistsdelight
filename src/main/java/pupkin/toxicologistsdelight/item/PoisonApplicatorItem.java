package pupkin.toxicologistsdelight.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

public class PoisonApplicatorItem extends Item
{
	public PoisonApplicatorItem(Properties p_41383_)
	{
		super(p_41383_);
	}
	
	public PoisonApplicatorItem()
	{
		super(new Properties()
				      .stacksTo(16)
		     );
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack applicator = player.getItemInHand(hand);
		ItemStack offhandItem = player.getOffhandItem();
		
		if (offhandItem.isEdible()) {
			ItemStack poisonedFood = PoisonUtils.poisonFood(offhandItem.copy(), 1);
			if (!PoisonUtils.isPoisoned(player.getOffhandItem())) {
				if (!level.isClientSide()) { // Can I stop adding nested if's already?
					applicator.shrink(1);
					player.setItemInHand(InteractionHand.OFF_HAND, poisonedFood);
					return InteractionResultHolder.success(applicator);
				}
			}
		}
		
		return super.use(level, player, hand);
	}
}

package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.item.SkilletItem;

// A held skillet assembles its campfire recipe from an empty container, so AbstractCookingRecipeMixin finds no ingredient
@Mixin(value = SkilletItem.class, remap = false)
public abstract class SkilletItemMixin
{
	// FD's static lambda in finishUsingItem: (Level, Player, ItemStack skillet, CampfireCookingRecipe)
	// ItemStack locals are the skillet argument (ordinal 0) and the assembled result (ordinal 1)
	@ModifyVariable(method = "/^lambda\\$finishUsingItem\\$\\d+$/", at = @At("STORE"), ordinal = 1)
	private static ItemStack toxicologistsdelight$poisonCooked(ItemStack result, Level level, Player player, ItemStack skillet)
	{
		CompoundTag tag = skillet.getTag();
		if (tag != null) PoisonUtils.propagate(Poison.of(tag.getCompound("Cooking").getCompound("tag")), result);
		return result;
	}
}

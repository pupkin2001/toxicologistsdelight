package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

// The stove spawns a copy of the campfire recipe's template result, so AbstractCookingRecipeMixin never sees it.
// Each slot's poison is read and put on that slot's result
@Mixin(value = StoveBlockEntity.class, remap = false)
public abstract class StoveBlockEntityMixin
{
	// Poison of the slot the cookAndOutputItems loop is at.
	@Unique
	@Nullable
	private Poison toxicologistsdelight$slotPoison;
	
	// First ItemStack local is the slot's stack
	@ModifyVariable(method = "cookAndOutputItems()V", at = @At("STORE"), ordinal = 0)
	private ItemStack toxicologistsdelight$readSlotPoison(ItemStack stoveStack)
	{
		toxicologistsdelight$slotPoison = Poison.of(stoveStack);
		return stoveStack;
	}
	
	// Spawned stack is FD's own copy of the template, so it can be changed
	@ModifyArg(method = "cookAndOutputItems()V",
	           at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/utility/ItemUtils;spawnItemEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;DDDDDD)V"),
	           index = 1)
	private ItemStack toxicologistsdelight$poisonCooked(ItemStack cooked)
	{
		PoisonUtils.propagate(toxicologistsdelight$slotPoison, cooked);
		return cooked;
	}
}

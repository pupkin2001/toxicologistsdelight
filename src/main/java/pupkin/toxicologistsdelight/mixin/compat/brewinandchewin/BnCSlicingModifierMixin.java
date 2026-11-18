package pupkin.toxicologistsdelight.mixin.compat.brewinandchewin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlockDropsModifier;
import umpaz.brewinandchewin.common.loot.modifier.BnCSlicingModifier;

// Wedges and slices of a cheese wheel or pizza broken with a knife (or a whole cheese wheel), in case B&C's slicing modifier runs after PoisonedBlockDropsModifier.
// Ripe cheese wheels and pizza have no loot table, so this is all they drop
@Mixin(value = BnCSlicingModifier.class, remap = false)
public abstract class BnCSlicingModifierMixin
{
	@Inject(method = "doApply(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$poisonSlices(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir)
	{
		PoisonedBlockDropsModifier.poisonDrops(cir.getReturnValue(), context);
	}
}

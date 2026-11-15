package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.compat.farmersdelight.PoisonedBlockDropsModifier;
import vectorwing.farmersdelight.common.loot.modifier.PastrySlicingModifier;

// Slices of a pie broken with a knife, in case FD's slicing modifier runs after PoisonedBlockDropsModifier. Also used by Brewin' and Chewin's quiche.
@Mixin(value = PastrySlicingModifier.class, remap = false)
public abstract class PastrySlicingModifierMixin
{
	@Inject(method = "doApply(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
	        at = @At("RETURN"))
	private void toxicologistsdelight$poisonSlices(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir)
	{
		PoisonedBlockDropsModifier.poisonDrops(cir.getReturnValue(), context);
	}
}

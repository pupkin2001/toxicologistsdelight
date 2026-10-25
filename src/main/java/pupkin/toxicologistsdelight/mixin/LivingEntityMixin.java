package pupkin.toxicologistsdelight.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Mobs that eat on their own (i.e. foxes) skip LivingEntityUseItemEvent.Finish.
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
	@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
	        at = @At(value = "INVOKE", shift = At.Shift.AFTER,
	                 target = "Lnet/minecraft/world/entity/LivingEntity;addEatEffect(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V"))
	private void toxicologistsdelight$poisonOnEat(Level level, ItemStack food, CallbackInfoReturnable<ItemStack> cir)
	{
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player || self.isUsingItem()) return;
		if (PoisonUtils.isConsumable(food)) PoisonUtils.applyPoison(self, food);
	}
}

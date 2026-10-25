package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.item.DogFoodItem;

// Farmer's Delight feeds dog food from an interaction event it then cancels, so vanilla's feeding (and AnimalMixin/WolfMixin) never runs.
// setHealth is only reached once the dog has accepted the food, before the stack is shrunk.
@Mixin(value = DogFoodItem.DogFoodEvent.class, remap = false)
public abstract class DogFoodEventMixin
{
	@Inject(method = "onDogFoodApplied(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$EntityInteract;)V",
	        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", remap = true))
	private static void toxicologistsdelight$poisonDog(PlayerInteractEvent.EntityInteract event, CallbackInfo ci)
	{
		if (event.getTarget() instanceof LivingEntity dog) PoisonUtils.applyPoison(dog, event.getItemStack());
	}
}

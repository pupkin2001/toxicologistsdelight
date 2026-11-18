package pupkin.toxicologistsdelight.mixin.compat.farmersdelight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pupkin.toxicologistsdelight.utils.PoisonUtils;
import vectorwing.farmersdelight.common.item.HorseFeedItem;

// Farmer's Delight feeds horse feed from an interaction event it then cancels, so vanilla's feeding (and AbstractHorseMixin) never runs.
// setHealth is only reached once the horse has accepted the feed, before its effects are applied and the stack is shrunk.
@Mixin(value = HorseFeedItem.HorseFeedEvent.class, remap = false)
public abstract class HorseFeedEventMixin
{
	@Inject(method = "onHorseFeedApplied(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent$EntityInteract;)V",
	        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", remap = true))
	private static void toxicologistsdelight$poisonHorse(PlayerInteractEvent.EntityInteract event, CallbackInfo ci)
	{
		if (event.getTarget() instanceof LivingEntity horse) PoisonUtils.applyPoison(horse, event.getItemStack());
	}
}

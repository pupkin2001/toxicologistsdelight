package pupkin.toxicologistsdelight.misc;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

// Brewing creates new potion stacks, so each slot's poison is read before the brew and written back onto its result
// Splash and lingering potions can't hold poison, so gunpowder and dragon's breath remove it
@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BrewingPoisonHandler
{
	private static final ThreadLocal<Poison[]> SLOT_POISONS = new ThreadLocal<>();
	
	private BrewingPoisonHandler() {}
	
	// Read the slots before any other handler can, even if one of them cancels brewing
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public static void onBrewStart(PotionBrewEvent.Pre event)
	{
		Poison[] poisons = new Poison[event.getLength()];
		for (int i = 0; i < poisons.length; i++) {
			poisons[i] = Poison.of(event.getItem(i));
		}
		SLOT_POISONS.set(poisons);
	}
	
	// Post holds the brewing stand's live slot list
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBrewed(PotionBrewEvent.Post event)
	{
		Poison[] poisons = SLOT_POISONS.get();
		SLOT_POISONS.remove();
		if (poisons == null) return;
		
		for (int i = 0; i < Math.min(poisons.length, event.getLength()); i++) {
			Poison poison = poisons[i];
			ItemStack stack = event.getItem(i);
			if (poison == null || poison.equals(Poison.of(stack)) || !PoisonUtils.canBePoisoned(stack)) continue;
			// Modded brewing recipe may return a shared stack, so poison a copy
			ItemStack poisoned = stack.copy();
			PoisonUtils.propagate(poison, poisoned);
			event.setItem(i, poisoned);
		}
	}
}

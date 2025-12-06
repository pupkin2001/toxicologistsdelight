package pupkin.toxicologistsdelight.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PoisonUtils
{
	public static final String POISONED_TAG = "poisoned";
	public static final String POISON_STRENGTH = "poison_strength";
	
	public static ItemStack poisonFood(@NotNull ItemStack stack, int strength)
	{
		CompoundTag tag = stack.getOrCreateTag();
		tag.putBoolean(POISONED_TAG, true);
		tag.putInt(POISON_STRENGTH, strength);
		return stack;
	}
	
	public static boolean isPoisoned(@NotNull ItemStack stack)
	{
		if (!stack.hasTag()) return false;
		assert stack.getTag() != null;
		return stack.getTag().getBoolean(POISONED_TAG);
	}
	
	public static int getPoisonStrength(@NotNull ItemStack stack)
	{
		if (!stack.hasTag()) return 0;
		assert stack.getTag() != null;
		return stack.getTag().getInt(POISON_STRENGTH);
	}
}

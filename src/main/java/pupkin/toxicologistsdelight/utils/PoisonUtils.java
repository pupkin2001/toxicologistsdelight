package pupkin.toxicologistsdelight.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;
import pupkin.toxicologistsdelight.item.AbstractVialItem;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;

public final class PoisonUtils
{
	private static final int TICKS_PER_STRENGTH = 600;
	/** Repeated doses of an active harmful poison raise its amplifier by the dose strength, but never past this. */
	private static final int MAX_DOSE_AMPLIFIER = 2;
	
	private PoisonUtils() {}
	
	/** Eaten or drunk through the normal item-use flow. Thrown potions and the mod's own vials don't count. */
	public static boolean isConsumable(@NotNull ItemStack stack)
	{
		if (stack.isEmpty()) return false;
		if (stack.getItem() instanceof ThrowablePotionItem || stack.getItem() instanceof AbstractVialItem) return false;
		if (stack.isEdible()) return true;
		UseAnim anim = stack.getUseAnimation();
		return anim == UseAnim.EAT || anim == UseAnim.DRINK;
	}
	
	/** Placed food (Farmer's Delight feasts and pies) whose servings carry the poison of the item it was placed from. */
	public static boolean isPoisonableBlock(@NotNull Block block)
	{
		return block instanceof FeastBlock || block instanceof PieBlock;
	}
	
	public static boolean isPoisonableBlockItem(@NotNull ItemStack stack)
	{
		return stack.getItem() instanceof BlockItem blockItem && isPoisonableBlock(blockItem.getBlock());
	}
	
	/** Whether a vial may poison this stack, and whether processing may carry poison onto it. */
	public static boolean canBePoisoned(@NotNull ItemStack stack)
	{
		return isConsumable(stack) || isPoisonableBlockItem(stack);
	}
	
	public static ItemStack poisonItem(@NotNull ItemStack stack, @NotNull MobEffect effect, int strength)
	{
		Poison poison = Poison.of(effect, strength);
		if (poison != null) poison.applyTo(stack); // refuse to write garbage
		return stack;
	}
	
	/** Carries {@code poison} onto a freshly made {@code result}, if there is one and the result can hold it. */
	public static void propagate(@Nullable Poison poison, @NotNull ItemStack result)
	{
		if (poison != null && canBePoisoned(result)) poison.applyTo(result);
	}
	
	public static boolean isPoisoned(@NotNull ItemStack stack)
	{
		return Poison.of(stack) != null;
	}
	
	public static void applyPoison(@NotNull LivingEntity target, @NotNull ItemStack source)
	{
		if (target.level().isClientSide()) return;
		Poison poison = Poison.of(source);
		if (poison == null) return;
		MobEffect effect = poison.effect();
		if (effect == null) return;
		
		int amplifier = poison.strength() - 1;
		MobEffectInstance active = target.getEffect(effect);
		if (active != null && effect.getCategory() == MobEffectCategory.HARMFUL) {
			// Another dose of an active poison makes it stronger instead of just refreshing it. Helpful doses (vitality) only refresh.
			int stacked = Math.min(active.getAmplifier() + poison.strength(), MAX_DOSE_AMPLIFIER);
			amplifier = Math.max(active.getAmplifier(), Math.max(amplifier, stacked));
		}
		int duration = effect == ToxicologistsEffects.NECROTOXIN.get() ? -1 : poison.strength() * TICKS_PER_STRENGTH;
		target.addEffect(new MobEffectInstance(effect, duration, amplifier));
	}
	
	public static ItemStack cleanseItem(@NotNull ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			Poison.remove(tag);
			if (tag.isEmpty()) stack.setTag(null);
		}
		return stack;
	}
}

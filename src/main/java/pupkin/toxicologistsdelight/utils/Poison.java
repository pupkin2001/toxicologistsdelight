package pupkin.toxicologistsdelight.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

import java.util.Objects;

/**
 * Immutable poison carried by an item or a fluid. The effect id and strength stored under {@link #TYPE_KEY} / {@link #STRENGTH_KEY} at the root of the stack's tag. Two poisons are
 * interchangeable (stacks may merge) only when they are {@code equals}.
 */
public record Poison(@NotNull ResourceLocation effectId, int strength)
{
	public static final String TYPE_KEY = "poison_type";
	public static final String STRENGTH_KEY = "poison_strength";
	
	public Poison
	{
		Objects.requireNonNull(effectId);
		strength = Math.max(1, strength);
	}
	
	@Nullable
	public static Poison of(@Nullable CompoundTag tag)
	{
		if (tag == null || !tag.contains(TYPE_KEY, Tag.TAG_STRING)) return null;
		ResourceLocation id = ResourceLocation.tryParse(tag.getString(TYPE_KEY));
		return id == null ? null : new Poison(id, tag.getInt(STRENGTH_KEY));
	}
	
	@Nullable
	public static Poison of(@NotNull ItemStack stack)
	{
		return of(stack.getTag());
	}
	
	/** Reads the tag even when the stack is empty - drained Forge tank keeps its stale tag at 0 mB */
	@Nullable
	public static Poison of(@NotNull FluidStack fluid)
	{
		return of(fluid.getTag());
	}
	
	@Nullable
	public static Poison of(@NotNull MobEffect effect, int strength)
	{
		ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
		return id == null || strength < 1 ? null : new Poison(id, strength);
	}
	
	/**
	 * The more dangerous of two poisons: Necrosis, then Putrefaction, then other harmful, neutral and beneficial effects, remaining are sorted by id.
	 */
	@Nullable
	public static Poison strongest(@Nullable Poison a, @Nullable Poison b)
	{
		if (a == null) return b;
		if (b == null) return a;
		int order = Integer.compare(a.severity(), b.severity());
		if (order == 0) order = Integer.compare(a.strength, b.strength);
		if (order == 0) order = b.effectId.compareTo(a.effectId);
		return order >= 0 ? a : b;
	}
	
	/** Strongest poison among all slots of {@code container} */
	@Nullable
	public static Poison strongestIn(@NotNull Container container)
	{
		Poison result = null;
		for (int i = 0; i < container.getContainerSize(); i++) {
			result = strongest(result, of(container.getItem(i)));
		}
		return result;
	}
	
	/** Strongest poison among slots {@code [from, to)} of {@code handler} */
	@Nullable
	public static Poison strongestIn(@NotNull IItemHandler handler, int from, int to)
	{
		Poison result = null;
		for (int i = from; i < Math.min(to, handler.getSlots()); i++) {
			result = strongest(result, of(handler.getStackInSlot(i)));
		}
		return result;
	}
	
	/** Removes the poison keys from {@code tag}, if present */
	public static void remove(@Nullable CompoundTag tag)
	{
		if (tag == null) return;
		tag.remove(TYPE_KEY);
		tag.remove(STRENGTH_KEY);
	}
	
	/** Same fluid and same tag once the poison keys are ignored on both sides */
	public static boolean fluidEqualIgnoringPoison(@NotNull FluidStack a, @NotNull FluidStack b)
	{
		if (a.getFluid() != b.getFluid()) return false;
		return Objects.equals(withoutPoison(a.getTag()), withoutPoison(b.getTag()));
	}
	
	@Nullable
	private static CompoundTag withoutPoison(@Nullable CompoundTag tag)
	{
		if (tag == null) return null;
		CompoundTag copy = tag.copy();
		remove(copy);
		return copy.isEmpty() ? null : copy;
	}
	
	@Nullable
	public MobEffect effect()
	{
		return ForgeRegistries.MOB_EFFECTS.getValue(effectId);
	}
	
	private int severity()
	{
		if (effectId.equals(ToxicologistsEffects.NECROTOXIN.getId())) return 4;
		if (effectId.equals(ToxicologistsEffects.ROT.getId())) return 3;
		MobEffect effect = effect();
		if (effect == null) return 0;
		return switch (effect.getCategory()) {
			case HARMFUL -> 2;
			case NEUTRAL -> 1;
			case BENEFICIAL -> 0;
		};
	}
	
	public void write(@NotNull CompoundTag tag)
	{
		tag.putString(TYPE_KEY, effectId.toString());
		tag.putInt(STRENGTH_KEY, strength);
	}
	
	public CompoundTag toTag()
	{
		CompoundTag tag = new CompoundTag();
		write(tag);
		return tag;
	}
	
	/** Writes this poison onto {@code stack} (replacing any other poison). Does nothing to an empty stack */
	public void applyTo(@NotNull ItemStack stack)
	{
		if (!stack.isEmpty()) write(stack.getOrCreateTag());
	}
	
	/** Writes this poison onto {@code fluid}. Does nothing to an empty stack, whose tag can't be written */
	public void applyTo(@NotNull FluidStack fluid)
	{
		if (!fluid.isEmpty()) write(fluid.getOrCreateTag());
	}
}

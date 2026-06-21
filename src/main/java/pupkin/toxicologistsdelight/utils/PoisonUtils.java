package pupkin.toxicologistsdelight.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.effect.ToxicologistsEffects;

public final class PoisonUtils
{
	public static final String POISON_TYPE = "poison_type";
	public static final String POISON_STRENGTH = "poison_strength";
	private static final int TICKS_PER_STRENGTH = 600;
	
	private PoisonUtils() {}
	
	public static ItemStack poisonItem(@NotNull ItemStack stack, @NotNull MobEffect effect, int strength)
	{
		ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
		if (id == null || strength < 1) return stack;             // refuse to write garbage
		CompoundTag tag = stack.getOrCreateTag();
		tag.putString(POISON_TYPE, id.toString());
		tag.putInt(POISON_STRENGTH, strength);
		return stack;
	}
	
	public static boolean isPoisoned(@NotNull ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains(POISON_TYPE, Tag.TAG_STRING);
	}
	
	public static int getPoisonStrength(@NotNull ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		return tag == null ? 0 : tag.getInt(POISON_STRENGTH);
	}
	
	@Nullable
	public static MobEffect getPoisonEffect(@NotNull ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(POISON_TYPE, Tag.TAG_STRING)) return null;
		ResourceLocation id = ResourceLocation.tryParse(tag.getString(POISON_TYPE));
		return id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
	}
	
	public static void applyPoison(@NotNull LivingEntity target, @NotNull ItemStack source)
	{
		if (target.level().isClientSide()) return;
		MobEffect effect = getPoisonEffect(source);
		if (effect == null) return;
		int strength = Math.max(1, getPoisonStrength(source));
		if (effect == ToxicologistsEffects.ACID.get()) {
			target.addEffect(new MobEffectInstance(effect, -1, strength - 1));
		} else {
			target.addEffect(new MobEffectInstance(effect, strength * TICKS_PER_STRENGTH, strength - 1));
		}
	}
	
	public static ItemStack cleanseItem(@NotNull ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			tag.remove(POISON_TYPE);
			tag.remove(POISON_STRENGTH);
			if (tag.isEmpty()) stack.setTag(null);
		}
		return stack;
	}
}

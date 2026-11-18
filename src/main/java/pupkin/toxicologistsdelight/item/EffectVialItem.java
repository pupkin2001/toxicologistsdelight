package pupkin.toxicologistsdelight.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.utils.Poison;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.function.Supplier;

public class EffectVialItem extends AbstractVialItem
{
	
	private final Supplier<MobEffect> effect;
	private final int strength;
	private final int batchSize;
	
	public EffectVialItem(Properties properties, Supplier<MobEffect> effect, int strength, int batchSize)
	{
		super(properties);
		this.effect = effect;
		this.strength = strength;
		this.batchSize = batchSize;
	}
	
	@Override
	public int getVialColor(ItemStack stack)
	{
		return effect.get().getColor();
	}
	
	@Override
	protected boolean canTransform(ItemStack target)
	{
		return PoisonUtils.canBePoisoned(target) && !PoisonUtils.isPoisoned(target);
	}
	
	@Override
	protected int batchSize()
	{
		return batchSize;
	}
	
	@Override
	protected ItemStack transform(ItemStack base)
	{
		return PoisonUtils.poisonItem(base, effect.get(), strength);
	}
	
	@Override
	protected boolean canTransformBlock(@Nullable Poison poison)
	{
		return poison == null && transformBlock() != null;
	}
	
	@Override
	protected @Nullable Poison transformBlock()
	{
		return Poison.of(effect.get(), strength);
	}
}

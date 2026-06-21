package pupkin.toxicologistsdelight.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

import java.util.function.Supplier;

public class EffectVialItem extends AbstractVialItem
{
	
	private final Supplier<MobEffect> effect; // supplier so the effect can register after the item
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
		return !PoisonUtils.isPoisoned(target);
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
}

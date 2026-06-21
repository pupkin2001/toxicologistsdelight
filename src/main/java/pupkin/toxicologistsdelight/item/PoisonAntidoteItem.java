package pupkin.toxicologistsdelight.item;

import net.minecraft.world.item.ItemStack;
import pupkin.toxicologistsdelight.utils.PoisonUtils;

public class PoisonAntidoteItem extends AbstractVialItem
{
	private static final int BATCH_SIZE = 8;
	
	public PoisonAntidoteItem()
	{
		super(new Properties().stacksTo(16));
	}
	
	@Override
	public int getVialColor(ItemStack stack)
	{
		return 0xFFFFFF;
	}
	
	@Override
	protected boolean canTransform(ItemStack target)
	{
		return PoisonUtils.isPoisoned(target);
	}
	
	@Override
	protected int batchSize()
	{
		return BATCH_SIZE;
	}
	
	@Override
	protected ItemStack transform(ItemStack base)
	{
		return PoisonUtils.cleanseItem(base);
	}
}

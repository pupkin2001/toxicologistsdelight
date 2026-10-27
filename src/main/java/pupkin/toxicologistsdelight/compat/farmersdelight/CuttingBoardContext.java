package pupkin.toxicologistsdelight.compat.farmersdelight;

import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.utils.Poison;

/**
 * Poison read by CuttingBoardRecipeMixin from item placed on the cutting board
 */
public final class CuttingBoardContext
{
	private static final ThreadLocal<Poison> PROCESSING = new ThreadLocal<>();
	
	private CuttingBoardContext() {}
	
	public static void begin(@Nullable Poison poison)
	{
		PROCESSING.set(poison);
	}
	
	public static void end()
	{
		PROCESSING.remove();
	}
	
	@Nullable
	public static Poison current()
	{
		return PROCESSING.get();
	}
}

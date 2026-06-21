package pupkin.toxicologistsdelight.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.DeferredRegister;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;

import java.util.List;
import java.util.Set;

public class TDItemModelProvider extends ItemModelProvider
{
	public TDItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, ToxicologistsDelight.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void registerModels()
	{
		List<DeferredRegister<Item>> registries = new java.util.ArrayList<>(List.of(
				ToxicologistsItems.ITEMS
		                                                                           ));
		
		registries.forEach(registry -> processRegistry(registry, Set.of("empty_vial")));
	}
	
	private void processRegistry(DeferredRegister<Item> registry, Set<String> skipNames)
	{
		registry.getEntries().stream().filter(e -> {
			assert e.getId() != null;
			return !skipNames.contains(e.getId().getPath());
		}).forEach(item -> {
			String itemName = item.getId().getPath();
			
			withExistingParent(itemName, "toxicologistsdelight:item/vial");
		});
	}
}
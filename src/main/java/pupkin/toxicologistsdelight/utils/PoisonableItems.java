package pupkin.toxicologistsdelight.utils;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pupkin.toxicologistsdelight.ToxicologistsDelight;
import pupkin.toxicologistsdelight.item.AbstractVialItem;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Items that can hold poison besides food itself, derived from recipes and tags.
 * See {@link PoisonUtils#canBePoisoned}.
 */
@Mod.EventBusSubscriber(modid = ToxicologistsDelight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PoisonableItems
{
	public static final TagKey<Item> POISONABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "poisonable"));
	public static final TagKey<Item> UNPOISONABLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ToxicologistsDelight.MOD_ID, "unpoisonable"));
	
	// Replaced as a whole, read from any thread
	private static volatile Set<Item> derived = Set.of();
	
	private PoisonableItems() {}
	
	public static boolean contains(@NotNull Item item)
	{
		return derived.contains(item);
	}
	
	/** Food itself, placed food and the poisonable tag */
	public static boolean isBase(@NotNull ItemStack stack)
	{
		return PoisonUtils.isConsumable(stack) || PoisonUtils.isFoodBlockItem(stack) || stack.is(POISONABLE);
	}
	
	/** Unpoisonable tag and vials. */
	public static boolean isExcluded(@NotNull ItemStack stack)
	{
		return stack.getItem() instanceof AbstractVialItem || stack.is(UNPOISONABLE);
	}
	
	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event)
	{
		recompute(event.getServer());
	}
	
	@SubscribeEvent
	public static void onDatapackSync(OnDatapackSyncEvent event)
	{
		if (event.getPlayer() == null) recompute(event.getPlayerList().getServer());
	}
	
	private static void recompute(@NotNull MinecraftServer server)
	{
		recompute(server.getRecipeManager(), server.registryAccess());
	}
	
	public static void recompute(@NotNull RecipeManager recipeManager, @NotNull RegistryAccess registryAccess)
	{
		Map<Fluid, List<Item>> pouredInto = new HashMap<>();
		for (Recipe<?> recipe : recipeManager.getRecipes()) {
			if (recipe instanceof KegPouringRecipe pouring) {
				pouredInto.computeIfAbsent(pouring.getRawFluid(), fluid -> new ArrayList<>()).add(pouring.getOutput().getItem());
			}
		}
		List<Shape> recipes = new ArrayList<>();
		for (Recipe<?> recipe : recipeManager.getRecipes()) {
			Shape shape = Shape.of(recipe, registryAccess, pouredInto);
			if (shape != null) recipes.add(shape);
		}
		
		Set<Item> result = new HashSet<>();
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack stack = item.getDefaultInstance();
			if (!stack.isEmpty() && !isExcluded(stack) && isBase(stack)) result.add(item);
		}
		
		// Ingredients of food, one level deep only
		Set<Item> ingredients = new HashSet<>();
		for (Shape recipe : recipes) {
			if (!recipe.makesAnyOf(result)) continue;
			for (Ingredient ingredient : recipe.ingredients()) {
				addItems(ingredient, ingredients);
			}
		}
		result.addAll(ingredients);
		
		// Whatever unpacks into poisonable items (onion crate -> onions, hay bale -> wheat, kelp -> dried kelp)
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Shape recipe : recipes) {
				if (recipe.ingredients().size() == 1 && recipe.makesAnyOf(result)) changed |= addItems(recipe.ingredients().get(0), result);
			}
		}
		
		derived = Set.copyOf(result);
		ToxicologistsDelight.LOGGER.info("{} items can be poisoned", result.size());
		if (ToxicologistsDelight.LOGGER.isDebugEnabled()) {
			ToxicologistsDelight.LOGGER.debug("Poisonable items: {}", result.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).sorted()
			                                                                 .collect(Collectors.joining(", ")));
		}
	}
	
	// Excluded items are never added, so they can't carry the derivation further
	private static boolean addItems(@NotNull Ingredient ingredient, @NotNull Set<Item> items)
	{
		boolean changed = false;
		for (ItemStack stack : ingredient.getItems()) {
			// Forge shows an empty tag as a barrier
			if (!stack.isEmpty() && !stack.is(Items.BARRIER) && !isExcluded(stack)) changed |= items.add(stack.getItem());
		}
		return changed;
	}
	
	/** What the derivation needs from a recipe, read once. */
	private record Shape(@NotNull List<Item> results, @NotNull List<Ingredient> ingredients)
	{
		@Nullable
		static Shape of(@NotNull Recipe<?> recipe, @NotNull RegistryAccess registryAccess, @NotNull Map<Fluid, List<Item>> pouredInto)
		{
			try {
				List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).toList();
				// A fermenting recipe makes either an item or a fluid
				Fluid fluid = recipe instanceof KegFermentingRecipe fermenting && fermenting.getResultItem() == null ? fermenting.getResultFluid() : null;
				List<Item> results = fluid != null ? pouredInto.getOrDefault(fluid, List.of()) : List.of(recipe.getResultItem(registryAccess).getItem());
				return new Shape(results, ingredients);
			} catch (RuntimeException e) {
				// Some recipes can't tell their result or ingredients outside crafting
				ToxicologistsDelight.LOGGER.debug("Skipping recipe {} for poisonable items", recipe.getId(), e);
				return null;
			}
		}
		
		boolean makesAnyOf(@NotNull Set<Item> items)
		{
			for (Item result : results) {
				if (items.contains(result)) return true;
			}
			return false;
		}
	}
}

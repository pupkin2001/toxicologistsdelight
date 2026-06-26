package pupkin.toxicologistsdelight.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import pupkin.toxicologistsdelight.item.ToxicologistsItems;
import pupkin.toxicologistsdelight.misc.ToxicologistsFluids;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class TDRecipeProvider extends RecipeProvider implements IConditionBuilder
{
	public static final int
			// Duration
			FAST_FERMENTING = 4800,      // 4 minutes
			NORMAL_FERMENTING = 9600,    // 8 minutes
			LONG_FERMENTING = 19200,     // 16 minutes
	
	// Temperature
	HOT_TEMPERATURE = 5,
			WARM_TEMPERATURE = 4,
			NORMAL_TEMPERATURE = 3,
			COLD_TEMPERATURE = 2,
			FRIGID_TEMPERATURE = 1;
	
	// Experience
	public static final float
			SMALL_EXP = 0.5F,
			MEDIUM_EXP = 1.0F,
			LARGE_EXP = 2.0F;
	
	public TDRecipeProvider(PackOutput output)
	{
		super(output);
	}
	
	@Override
	protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
	{
		generateFermentingRecipes(consumer);
		generatePouringRecipes(consumer);
		generateCookingRecipes(consumer);
		generateCraftingRecipes(consumer);
	}
	
	private void generateFermentingRecipes(Consumer<FinishedRecipe> consumer)
	{
		// Fermenting drinks
		
		// poison_tincture: 1000mb water + spider eye, hot, long ferment -> 250mb poison_tincture
		createFermentingRecipe(consumer, "poison_vial", "drinks",
		                       Fluids.WATER, 1000,
		                       LONG_FERMENTING, HOT_TEMPERATURE, 4.0F,
		                       ToxicologistsFluids.POISON_TINCTURE.source(), 250,
		                       Items.SPIDER_EYE);
		
		// necrotoxin: 1000mb milk + echo shard, fermented spider eye, pufferfish, red mushroom,
		//             warm, long ferment -> 250mb necrotoxin
		createFermentingRecipe(consumer, "necrotoxin_vial", "drinks",
		                       ResourceLocation.fromNamespaceAndPath("minecraft", "milk"), 1000,
		                       LONG_FERMENTING, WARM_TEMPERATURE, 4.0F,
		                       ToxicologistsFluids.NECROTOXIN.source(), 250,
		                       Items.ECHO_SHARD, Items.FERMENTED_SPIDER_EYE, Items.PUFFERFISH, Items.RED_MUSHROOM);
		
		// Fermenting meals
	}
	
	private void generatePouringRecipes(Consumer<FinishedRecipe> consumer)
	{
		// Pouring drinks
		createPouringRecipe(consumer, "antidote_vial",
		                    ToxicologistsFluids.ANTIDOTE.source().get(), 250,
		                    ToxicologistsItems.EMPTY_VIAL, ToxicologistsItems.ANTIDOTE_VIAL,
		                    true, false);
		
		createPouringRecipe(consumer, "necrotoxin_vial",
		                    ToxicologistsFluids.NECROTOXIN.source().get(), 250,
		                    ToxicologistsItems.EMPTY_VIAL, ToxicologistsItems.NECROTOXIN_VIAL,
		                    true, false);
		
		createPouringRecipe(consumer, "poison_vial",
		                    ToxicologistsFluids.POISON_TINCTURE.source().get(), 250,
		                    ToxicologistsItems.EMPTY_VIAL, ToxicologistsItems.POISON_VIAL,
		                    true, false);
	}
	
	private void generateCookingRecipes(Consumer<FinishedRecipe> consumer)
	{
		// antidote_vial: cook milk + honey bottle + charcoal + sweet berries in an empty vial -> antidote_vial
		createCookingRecipe(consumer, "antidote_vial", "meals",
		                    ToxicologistsItems.EMPTY_VIAL, 200, MEDIUM_EXP,
		                    ToxicologistsItems.ANTIDOTE_VIAL, 1,
		                    "forge:milk/milk", Items.HONEY_BOTTLE, Items.CHARCOAL, Items.SWEET_BERRIES);
	}
	
	private void generateCraftingRecipes(Consumer<FinishedRecipe> consumer)
	{
		// empty_vial: two stacked glass panes -> 2 empty vials
		createShapedRecipe(consumer, "empty_vial",
		                   ToxicologistsItems.EMPTY_VIAL, 2,
		                   List.of("g", "g"),
		                   Map.of('g', "forge:glass_panes"));
	}
	
	// Shared ingredient serializer used by fermenting, cooking, and shaped key entries.
	private static void writeIngredient(JsonObject ingredientJson, Object ingredient)
	{
		if (ingredient instanceof Item item) {
			// Regular item
			ingredientJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());
		} else if (ingredient instanceof net.minecraft.tags.TagKey<?> tagKey) {
			// Item tag
			ingredientJson.addProperty("tag", tagKey.location().toString());
		} else if (ingredient instanceof String s) {
			// String representation of a tag
			ingredientJson.addProperty("tag", s);
		} else if (ingredient instanceof RegistryObject<?> regObj) {
			// RegistryObject for Item
			Item item = (Item) regObj.get();
			ingredientJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).toString());
		} else {
			throw new IllegalArgumentException("Ingredient must be an Item, TagKey<Item>, String tag representation, or RegistryObject<Item>: " + ingredient);
		}
	}
	
	private void createFermentingRecipe(@NotNull Consumer<FinishedRecipe> consumer, String recipeName, String recipeBookTab,
	                                    Object baseFluid, int baseFluidCount,
	                                    int fermentingTime, int temperature, float experience,
	                                    ResourceLocation resultFluid, int resultFluidCount,
	                                    Object... ingredients)
	{
		consumer.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@NotNull JsonObject json)
			{
				JsonObject baseFluidJson = new JsonObject();
				baseFluidJson.addProperty("count", baseFluidCount);
				
				if (baseFluid instanceof Fluid) {
					// Regular fluid
					baseFluidJson.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey((Fluid) baseFluid)).toString());
				} else if (baseFluid instanceof net.minecraft.tags.TagKey) {
					// Fluid tag
					net.minecraft.tags.TagKey<Fluid> tagKey = (net.minecraft.tags.TagKey<Fluid>) baseFluid;
					baseFluidJson.addProperty("tag", tagKey.location().toString());
				} else if (baseFluid instanceof String) {
					// String representation of fluid tag
					baseFluidJson.addProperty("tag", (String) baseFluid);
				} else if (baseFluid instanceof ResourceLocation) {
					// Direct ResourceLocation for fluid
					baseFluidJson.addProperty("fluid", baseFluid.toString());
				} else if (baseFluid instanceof RegistryObject<?> regObj) {
					// RegistryObject for Fluid or FlowingFluid
					Fluid fluid = (Fluid) regObj.get();
					baseFluidJson.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).toString());
				} else {
					throw new IllegalArgumentException(
							"Base fluid must be a Fluid, TagKey<Fluid>, String tag representation, ResourceLocation, or RegistryObject<Fluid>: " + baseFluid);
				}
				
				json.add("basefluid", baseFluidJson);
				
				JsonArray ingredientsJson = new JsonArray();
				for (Object ingredient : ingredients) {
					JsonObject ingredientJson = new JsonObject();
					writeIngredient(ingredientJson, ingredient);
					ingredientsJson.add(ingredientJson);
				}
				json.add("ingredients", ingredientsJson);
				
				json.addProperty("fermentingtime", fermentingTime);
				json.addProperty("temperature", temperature);
				
				JsonObject resultJson = new JsonObject();
				resultJson.addProperty("count", resultFluidCount);
				resultJson.addProperty("fluid", resultFluid.toString());
				json.add("result", resultJson);
				
				json.addProperty("experience", experience);
				json.addProperty("recipe_book_tab", recipeBookTab);
			}
			
			@Override
			public @NotNull ResourceLocation getId()
			{
				return ResourceLocation.fromNamespaceAndPath("toxicologistsdelight", "fermenting/" + recipeName);
			}
			
			@Override
			public net.minecraft.world.item.crafting.@NotNull RecipeSerializer<?> getType()
			{
				return Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getValue(
						ResourceLocation.fromNamespaceAndPath("brewinandchewin", "fermenting")));
			}
			
			@Override
			public JsonObject serializeAdvancement()
			{
				return null; // No advancement
			}
			
			@Override
			public ResourceLocation getAdvancementId()
			{
				return null; // No advancement
			}
		});
	}
	
	// Wrapper
	private void createFermentingRecipe(Consumer<FinishedRecipe> consumer, String recipeName, String recipeBookTab,
	                                    Object baseFluid, int baseFluidCount,
	                                    int fermentingTime, int temperature, float experience,
	                                    Fluid resultFluid, int resultFluidCount,
	                                    Object... ingredients)
	{
		ResourceLocation resultFluidId = ForgeRegistries.FLUIDS.getKey(resultFluid);
		if (resultFluidId == null) {
			throw new IllegalStateException("Unable to create fermenting recipe for fluid " + resultFluid + " as it does not exist.");
		}
		
		createFermentingRecipe(consumer, recipeName, recipeBookTab,
		                       baseFluid, baseFluidCount,
		                       fermentingTime, temperature, experience,
		                       resultFluidId, resultFluidCount,
		                       ingredients);
	}
	
	// Wrapper
	private void createFermentingRecipe(Consumer<FinishedRecipe> consumer, String recipeName, String recipeBookTab,
	                                    Object baseFluid, int baseFluidCount,
	                                    int fermentingTime, int temperature, float experience,
	                                    RegistryObject<? extends Fluid> resultFluid, int resultFluidCount,
	                                    Object... ingredients)
	{
		ResourceLocation resultFluidId = ForgeRegistries.FLUIDS.getKey(resultFluid.get());
		if (resultFluidId == null) {
			throw new IllegalStateException("Unable to create fermenting recipe for fluid " + resultFluid + " as it does not exist.");
		}
		
		createFermentingRecipe(consumer, recipeName, recipeBookTab,
		                       baseFluid, baseFluidCount,
		                       fermentingTime, temperature, experience,
		                       resultFluidId, resultFluidCount,
		                       ingredients);
	}
	
	private void createPouringRecipe(@NotNull Consumer<FinishedRecipe> consumer, String recipeName,
	                                 ResourceLocation fluid, int fluidAmount,
	                                 ResourceLocation container, ResourceLocation output,
	                                 boolean filling, boolean strict)
	{
		consumer.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@NotNull JsonObject json)
			{
				json.addProperty("type", "brewinandchewin:keg_pouring");
				json.addProperty("amount", fluidAmount);
				
				// Container
				JsonObject containerJson = new JsonObject();
				containerJson.addProperty("item", container.toString());
				json.add("container", containerJson);
				
				json.addProperty("filling", filling);
				json.addProperty("fluid", fluid.toString());
				
				// Output
				JsonObject outputJson = new JsonObject();
				outputJson.addProperty("item", output.toString());
				json.add("output", outputJson);
				
				json.addProperty("strict", strict);
			}
			
			@Override
			public @NotNull ResourceLocation getId()
			{
				return ResourceLocation.fromNamespaceAndPath("toxicologistsdelight", "pouring/" + recipeName);
			}
			
			@Override
			public net.minecraft.world.item.crafting.@NotNull RecipeSerializer<?> getType()
			{
				return Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getValue(
						ResourceLocation.fromNamespaceAndPath("brewinandchewin", "keg_pouring")));
			}
			
			@Override
			public JsonObject serializeAdvancement()
			{
				return null; // No advancement
			}
			
			@Override
			public ResourceLocation getAdvancementId()
			{
				return null; // No advancement
			}
		});
	}
	
	public void createPouringRecipe(Consumer<FinishedRecipe> consumer, String recipeName,
	                                Fluid fluid, int fluidAmount,
	                                @NotNull RegistryObject<Item> containerItem, @NotNull RegistryObject<Item> outputItem,
	                                boolean filling, boolean strict)
	{
		ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
		ResourceLocation containerId = containerItem.getId();
		ResourceLocation outputId = outputItem.getId();
		
		if (fluidId == null) {
			throw new IllegalStateException("Unable to create pouring recipes for fluid " + fluid + " as it does not exist.");
		}
		
		Objects.requireNonNull(containerItem, "pouring recipe needs a container item");
		assert outputItem.getId() != null;
		createPouringRecipe(consumer, outputItem.getId().getPath(),
		                    fluidId, fluidAmount,
		                    containerItem.getId(),
		                    outputItem.getId(),
		                    filling, strict);
	}
	
	private void createCookingRecipe(@NotNull Consumer<FinishedRecipe> consumer, String recipeName, String recipeBookTab,
	                                 ResourceLocation container, int cookingTime, float experience,
	                                 ResourceLocation result, int resultCount,
	                                 Object... ingredients)
	{
		consumer.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@NotNull JsonObject json)
			{
				// container is optional in Farmer's Delight cooking recipes
				if (container != null) {
					JsonObject containerJson = new JsonObject();
					containerJson.addProperty("item", container.toString());
					json.add("container", containerJson);
				}
				
				json.addProperty("cookingtime", cookingTime);
				json.addProperty("experience", experience);
				
				JsonArray ingredientsJson = new JsonArray();
				for (Object ingredient : ingredients) {
					JsonObject ingredientJson = new JsonObject();
					writeIngredient(ingredientJson, ingredient);
					ingredientsJson.add(ingredientJson);
				}
				json.add("ingredients", ingredientsJson);
				
				json.addProperty("recipe_book_tab", recipeBookTab);
				
				JsonObject resultJson = new JsonObject();
				resultJson.addProperty("item", result.toString());
				if (resultCount > 1) {
					resultJson.addProperty("count", resultCount);
				}
				json.add("result", resultJson);
			}
			
			@Override
			public @NotNull ResourceLocation getId()
			{
				return ResourceLocation.fromNamespaceAndPath("toxicologistsdelight", "cooking/" + recipeName);
			}
			
			@Override
			public net.minecraft.world.item.crafting.@NotNull RecipeSerializer<?> getType()
			{
				return Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getValue(
						ResourceLocation.fromNamespaceAndPath("farmersdelight", "cooking")));
			}
			
			@Override
			public JsonObject serializeAdvancement()
			{
				return null; // No advancement
			}
			
			@Override
			public ResourceLocation getAdvancementId()
			{
				return null; // No advancement
			}
		});
	}
	
	// Wrapper
	private void createCookingRecipe(Consumer<FinishedRecipe> consumer, String recipeName, String recipeBookTab,
	                                 RegistryObject<Item> container, int cookingTime, float experience,
	                                 RegistryObject<Item> result, int resultCount,
	                                 Object... ingredients)
	{
		createCookingRecipe(consumer, recipeName, recipeBookTab,
		                    container == null ? null : container.getId(), cookingTime, experience,
		                    result.getId(), resultCount,
		                    ingredients);
	}
	
	private void createShapedRecipe(@NotNull Consumer<FinishedRecipe> consumer, String recipeName,
	                                ResourceLocation result, int resultCount,
	                                List<String> pattern, Map<Character, ?> key)
	{
		consumer.accept(new FinishedRecipe()
		{
			@Override
			public void serializeRecipeData(@NotNull JsonObject json)
			{
				JsonArray patternJson = new JsonArray();
				for (String row : pattern) {
					patternJson.add(row);
				}
				json.add("pattern", patternJson);
				
				JsonObject keyJson = new JsonObject();
				for (Map.Entry<Character, ?> entry : key.entrySet()) {
					JsonObject ingredientJson = new JsonObject();
					writeIngredient(ingredientJson, entry.getValue());
					keyJson.add(String.valueOf(entry.getKey()), ingredientJson);
				}
				json.add("key", keyJson);
				
				JsonObject resultJson = new JsonObject();
				resultJson.addProperty("item", result.toString());
				if (resultCount > 1) {
					resultJson.addProperty("count", resultCount);
				}
				json.add("result", resultJson);
			}
			
			@Override
			public @NotNull ResourceLocation getId()
			{
				// Crafting recipes go in the main recipes folder (no subdir).
				return ResourceLocation.fromNamespaceAndPath("toxicologistsdelight", recipeName);
			}
			
			@Override
			public net.minecraft.world.item.crafting.@NotNull RecipeSerializer<?> getType()
			{
				return RecipeSerializer.SHAPED_RECIPE;
			}
			
			@Override
			public JsonObject serializeAdvancement()
			{
				return null; // No advancement
			}
			
			@Override
			public ResourceLocation getAdvancementId()
			{
				return null; // No advancement
			}
		});
	}
	
	// Wrapper
	private void createShapedRecipe(Consumer<FinishedRecipe> consumer, String recipeName,
	                                RegistryObject<Item> result, int resultCount,
	                                List<String> pattern, Map<Character, ?> key)
	{
		createShapedRecipe(consumer, recipeName, result.getId(), resultCount, pattern, key);
	}
}
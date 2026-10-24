package pupkin.toxicologistsdelight.compat;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/** Skips compat mixins whose target code isn't the one they were written for. */
public final class ToxicologistsMixinPlugin implements IMixinConfigPlugin
{
	private static final Logger LOGGER = LoggerFactory.getLogger("toxicologistsdelight");
	private static final String KEG_POUR_COUNT_MIXIN = "pupkin.toxicologistsdelight.mixin.compat.brewinandchewin.KegPourCountMixin";
	private static final String KEG_POUR_METHOD = "fluidExtract";
	private static final String KEG_POUR_DESCRIPTOR = "(Lnet/minecraft/world/item/ItemStack;IZZ)Ljava/util/List;";
	
	private static boolean hasMethod(String className, String name, String descriptor)
	{
		try {
			ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(className);
			for (MethodNode method : node.methods) {
				if (method.name.equals(name) && method.desc.equals(descriptor)) return true;
			}
		} catch (Exception e) {
			LOGGER.warn("Couldn't inspect {} for compat mixins", className, e);
		}
		return false;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		if (!KEG_POUR_COUNT_MIXIN.equals(mixinClassName)) return true;
		return hasMethod(targetClassName, KEG_POUR_METHOD, KEG_POUR_DESCRIPTOR);
	}
	
	@Override
	public void onLoad(String mixinPackage) {}
	
	@Override
	public String getRefMapperConfig() { return null; }
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	
	@Override
	public List<String> getMixins() { return null; }
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

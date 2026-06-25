package pupkin.toxicologistsdelight.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractVialItem extends Item implements ColoredVial
{
	
	private static final int TARGET_COOLDOWN_TICKS = 20;
	private static final int VIAL_COOLDOWN_TICKS = 10;
	
	/** Radius (in blocks) of the particle cloud spawned at the hand. */
	private static final double PARTICLE_CLOUD_RADIUS = 0.25;
	
	protected AbstractVialItem(Properties properties)
	{
		super(properties);
	}
	
	private static boolean isConsumable(ItemStack stack)
	{
		return stack.isEdible() || stack.getItem() instanceof PotionItem;
	}
	
	private static void giveOrDrop(Player player, ItemStack stack)
	{
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}
	
	/** Extra condition the off-hand target must meet (e.g. poisoned / not poisoned). */
	protected abstract boolean canTransform(ItemStack target);
	
	/** Maximum number of items from one stack a single use modifies. */
	protected abstract int batchSize();
	
	/** Transforms {@code base} (already sized to the batch) into the result stack. */
	protected abstract ItemStack transform(ItemStack base);
	
	/**
	 * Sound played server-side when the vial is successfully used.
	 * Defaults to a vanilla glass-bottle sound.
	 */
	protected SoundEvent useSound()
	{
		return SoundEvents.BOTTLE_EMPTY;
	}
	
	/** Volume of {@link #useSound()}. */
	protected float useSoundVolume()
	{
		return 1.0F;
	}
	
	/** Pitch of {@link #useSound()}. */
	protected float useSoundPitch()
	{
		return 1.0F;
	}
	
	/** Number of particles in the cloud spawned at the off-hand.  */
	protected int useParticleCount()
	{
		return 12;
	}
	
	/**
	 * Particle used when {@link #recolorParticles()} is {@code false}. When recolouring
	 * is on, the vanilla colour-capable {@code ENTITY_EFFECT} is used instead so it can
	 * be tinted. Override to change the un-tinted particle.
	 */
	protected ParticleOptions useParticle()
	{
		return ParticleTypes.EFFECT;
	}
	
	/** Whether particles are tinted to {@link #particleColor(ItemStack)}. */
	protected boolean recolorParticles()
	{
		return true;
	}
	
	/**
	 * Packed 0xRRGGBB colour used to tint particles when {@link #recolorParticles()} is on.
	 * Defaults to the vial's own colour from {@link ColoredVial}.
	 */
	protected int particleColor(ItemStack vial)
	{
		return getVialColor(vial);
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand)
	{
		ItemStack vial = player.getItemInHand(hand);
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.pass(vial);
		}
		
		ItemStack target = player.getOffhandItem();
		if (player.getCooldowns().isOnCooldown(vial.getItem())
				|| player.getCooldowns().isOnCooldown(target.getItem())) {
			return InteractionResultHolder.pass(vial);
		}
		if (!isConsumable(target) || !canTransform(target)) {
			return InteractionResultHolder.pass(vial);
		}
		
		player.getCooldowns().addCooldown(target.getItem(), TARGET_COOLDOWN_TICKS);
		player.getCooldowns().addCooldown(this, VIAL_COOLDOWN_TICKS);
		
		if (!level.isClientSide()) {
			transformOffhand(player, target);
			playUseSound(level, player);
			spawnUseParticles((ServerLevel) level, player, vial); // read colour before the vial shrinks
			consumeVial(player, vial);
		}
		return InteractionResultHolder.sidedSuccess(vial, level.isClientSide());
	}
	
	private void playUseSound(Level level, Player player)
	{
		level.playSound(
				null,
				player.getX(), player.getY(), player.getZ(),
				useSound(),
				SoundSource.PLAYERS,
				useSoundVolume(),
				useSoundPitch());
	}
	
	private void spawnUseParticles(ServerLevel level, Player player, ItemStack vial)
	{
		int count = useParticleCount();
		if (count <= 0) {
			return;
		}
		Vec3 pos = offHandPos(player);
		
		if (recolorParticles()) {
			int color = particleColor(vial);
			double r = (color >> 16 & 0xFF) / 255.0;
			double g = (color >> 8 & 0xFF) / 255.0;
			double b = (color & 0xFF) / 255.0;
			RandomSource random = level.getRandom();
			for (int i = 0; i < count; i++) {
				double ox = (random.nextDouble() * 2.0 - 1.0) * PARTICLE_CLOUD_RADIUS;
				double oy = (random.nextDouble() * 2.0 - 1.0) * PARTICLE_CLOUD_RADIUS;
				double oz = (random.nextDouble() * 2.0 - 1.0) * PARTICLE_CLOUD_RADIUS;
				// ENTITY_EFFECT reads (r,g,b) from the velocity args as its colour, so count MUST be 0.
				level.sendParticles(ParticleTypes.ENTITY_EFFECT,
				                    pos.x + ox, pos.y + oy, pos.z + oz, 0, r, g, b, 1.0);
			}
		} else {
			double spread = PARTICLE_CLOUD_RADIUS * 0.5;
			level.sendParticles(useParticle(), pos.x, pos.y, pos.z, count, spread, spread, spread, 0.0);
		}
	}
	
	/** Approximate world position of the off-hand (the left hand on a default model). */
	// TODO: account for left hand being main hand
	private static Vec3 offHandPos(Player player)
	{
		double yaw = Math.toRadians(player.getYRot());
		double leftX = Math.cos(yaw);   // unit vector pointing to the player's left
		double leftZ = Math.sin(yaw);
		double fwdX = -Math.sin(yaw);   // horizontal facing vector
		double fwdZ = Math.cos(yaw);
		
		double side = 0.4;
		double front = 0.1;
		return new Vec3(
				player.getX() + leftX * side + fwdX * front,
				player.getEyeY() - 0.45, // roughly hand height
				player.getZ() + leftZ * side + fwdZ * front);
	}
	
	private void transformOffhand(Player player, ItemStack target)
	{
		int batch = Math.min(batchSize(), target.getCount());
		int remainder = target.getCount() - batch;
		
		ItemStack base = target.copy();
		base.setCount(batch);
		ItemStack result = transform(base);
		
		if (remainder > 0) {
			ItemStack keep = target.copy();
			keep.setCount(remainder);
			player.setItemInHand(InteractionHand.OFF_HAND, keep);
			giveOrDrop(player, result);
		} else {
			player.setItemInHand(InteractionHand.OFF_HAND, result);
		}
	}
	
	private void consumeVial(Player player, ItemStack vial)
	{
		if (player.getAbilities().instabuild) {
			return;
		}
		vial.shrink(1);
		giveOrDrop(player, new ItemStack(ToxicologistsItems.EMPTY_VIAL.get()));
	}
}
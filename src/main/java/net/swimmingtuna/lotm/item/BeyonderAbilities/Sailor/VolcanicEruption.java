package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.LavaEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VolcanicEruption extends SimpleAbilityItem {

    public VolcanicEruption(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 2, 600, 400);
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 35, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        volcanicEruption(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }

            addCooldown(player);
            useSpirituality(player);
            volcanicEruptionTarget(player, interactionTarget);
        }
        return InteractionResult.SUCCESS;
    }

    public static void volcanicEruption(LivingEntity living) {
        if (!living.level().isClientSide()) {
            List<LivingEntity> entitiesInRange = living.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(40 - (BeyonderUtil.getSequence(living))));
            List<LivingEntity> validTargets = entitiesInRange.stream().filter(entity -> entity != living && !BeyonderUtil.areAllies(entity, living)).collect(Collectors.toList());

            validTargets.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(living)));
            int targetCount = Math.min(10, validTargets.size());

            for (int i = 0; i < targetCount; i++) {
                LivingEntity target = validTargets.get(i);

                if (target instanceof Mob mob && mob.getTarget() == null) {
                    mob.setTarget(living);
                }

                int damage = (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.VOLCANIC_ERUPTION.get());
                target.getPersistentData().putInt("volcanicEruption", damage);

                EventManager.addToRegularLoop(target, EFunctions.VOLCANIC_ERUPTION.get());
            }
        }
    }

    public static void volcanicEruptionTarget(LivingEntity living, LivingEntity target) {
        if (!living.level().isClientSide()) {
            target.getPersistentData().putInt("volcanicEruption",
                    (int) (float) BeyonderUtil.getDamage(living).get(ItemInit.VOLCANIC_ERUPTION.get()) * 2);

            EventManager.addToRegularLoop(target, EFunctions.VOLCANIC_ERUPTION.get());
        }
    }

    public static void volcanicEruptionTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();

        if (!living.level().isClientSide() && living.getPersistentData().getInt("volcanicEruption") >= 1) {
            Level level = living.level();
            living.getPersistentData().putInt("volcanicEruption", living.getPersistentData().getInt("volcanicEruption") - 1);

            float damage = 1;
            double randomX = (Math.random() * 1) - 0.5;
            double randomZ = (Math.random() * 1) - 0.5;
            Random random = new Random();
            BlockPos playerPos = living.blockPosition();

            for (int i = 0; i < damage; i++) {
                int offsetX = random.nextInt(21) - 10;
                int offsetZ = random.nextInt(21) - 10;
                BlockPos spawnPos = playerPos.offset(offsetX, 0, offsetZ);

                while (level.isEmptyBlock(spawnPos) && spawnPos.getY() > level.getMinBuildHeight()) {
                    spawnPos = spawnPos.below();
                }

                if (!level.isEmptyBlock(spawnPos) && isOnSurface(level, spawnPos)) {
                    LavaEntity lavaEntity = new LavaEntity(EntityInit.LAVA_ENTITY.get(), level);

                    lavaEntity.teleportTo(spawnPos.getX(), spawnPos.getY() + 3, spawnPos.getZ());

                    lavaEntity.setDeltaMovement(randomX, 3 + (Math.random() * 3), randomZ);
                    lavaEntity.setLavaXRot(random.nextInt(18));
                    lavaEntity.setLavaYRot(random.nextInt(18));

                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(lavaEntity);
                    scaleData.setScale(1.0f + random.nextFloat() * 2.0f);

                    level.addFreshEntity(lavaEntity);
                }
            }
        }
        else{
            EventManager.removeFromRegularLoop(living, EFunctions.VOLCANIC_ERUPTION.get());
        }
    }

    private static boolean isOnSurface(Level level, BlockPos pos) {
        return level.canSeeSky(pos.above()) || !level.getBlockState(pos.above()).isSolid();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons a volcanic eruption from under the 10 nearest entities near you. You can also target this on a specific entity to make the eruption last twice as long."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("600").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("20 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 10;
        }
        return 0;
    }

}

package net.swimmingtuna.lotm.item.BeyonderAbilities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.capabilities.sealed_data.ABILITIES_SEAL_TYPES;
import net.swimmingtuna.lotm.capabilities.sealed_data.SealedUtils;
import net.swimmingtuna.lotm.init.GameRuleInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientWormOfStarDataS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SimpleAbilityItem extends Item implements Ability {
    protected final Supplier<? extends BeyonderClass> requiredClass;
    protected final int requiredSequence;
    protected final int requiredSpirituality;
    protected final int cooldown;
    protected final double entityReach;
    protected final double blockReach;
    protected final int priority = 0;

    protected SimpleAbilityItem(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        this(properties, () -> requiredClass, requiredSequence, requiredSpirituality, cooldown, 3.0, 4.5);
    }

    protected SimpleAbilityItem(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown) {
        this(properties, requiredClass, requiredSequence, requiredSpirituality, cooldown, 3.0, 4.5);
    }

    protected SimpleAbilityItem(Properties properties, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        this(properties, () -> requiredClass, requiredSequence, requiredSpirituality, cooldown, entityReach, blockReach);
    }

    protected SimpleAbilityItem(Properties properties, Supplier<? extends BeyonderClass> requiredClass, int requiredSequence, int requiredSpirituality, int cooldown, double entityReach, double blockReach) {
        super(properties);
        this.requiredClass = requiredClass;
        this.requiredSequence = requiredSequence;
        this.requiredSpirituality = requiredSpirituality;
        this.cooldown = cooldown;
        this.entityReach = entityReach;
        this.blockReach = blockReach;
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return priority;
    }

    @Override
    public double getBlockReach() {
        return blockReach;
    }

    @Override
    public double getEntityReach() {
        return entityReach;
    }


    public boolean checkAll(LivingEntity living) {
        if (living instanceof Player player && player.getCooldowns().isOnCooldown(this)) {
            return false;
        } else {
            if (living.getPersistentData().getInt("abilityCooldownFor" + this.getDescription().getString()) >= 1) {
                return false;
            }
        }
        boolean itemCheckPassed = true;
        boolean isKeybindUse = !(living.getItemInHand(InteractionHand.MAIN_HAND).is(this) || living.getItemInHand(InteractionHand.OFF_HAND).is(this));
        if (!isKeybindUse && living instanceof Player) {
            itemCheckPassed = living.getItemInHand(InteractionHand.MAIN_HAND).is(this) ||
                    living.getItemInHand(InteractionHand.OFF_HAND).is(this);
        }
        if (itemCheckPassed) {
            boolean checkAllResult = checkAll(living, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality, false);
            if (!checkAllResult) {
                boolean sequenceAble = BeyonderUtil.sequenceAbleCopy(living);
                if (sequenceAble) {
                    boolean abilityCopied = BeyonderUtil.checkAbilityIsCopied(living, this);
                    if (abilityCopied) {
                        BeyonderUtil.useCopiedAbility(living, this);
                        return checkSpirituality(living, this.getSpirituality(), true);
                    }
                }
            }
            boolean finalCheck = checkAll(living, this.requiredClass.get(), this.requiredSequence, this.requiredSpirituality, true);
            if (finalCheck) {
                BeyonderUtil.copyAbilities(living.level(), living, this);
                return true;
            }
        }
        return false;
    }


    public boolean abilitiesArentSealed(LivingEntity living, Item abilityItem) {
        if (!living.level().isClientSide()) {
            if(SealedUtils.hasAbilitiesSealed(living)){
                if(abilityItem instanceof SimpleAbilityItem ability) {
                    HashSet<ABILITIES_SEAL_TYPES> types = SealedUtils.getSealedAbilitiesTypes(living);
                    if (types.contains(ABILITIES_SEAL_TYPES.ALL)) {
                        if (living instanceof Player player) {
                            player.displayClientMessage(Component.literal("All your abilities are sealed").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
                        }
                        return false;
                    } else if (types.contains(ABILITIES_SEAL_TYPES.LIST)) {
                        if (SealedUtils.getAllSealedAbilitiesListType(living).contains(ability)) {
                            if (living instanceof Player player) {
                                player.displayClientMessage(Component.literal("This ability is sealed").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
                            }
                            return false;
                        }
                    } else if (types.contains(ABILITIES_SEAL_TYPES.SEQUENCE)){
                        if (SealedUtils.getAllSealedAbilitiesSequenceType(living).contains(ability.requiredSequence)){
                            if (living instanceof Player player) {
                                player.displayClientMessage(Component.literal("All your abilities for sequence " + ability.getRequiredSequence() + " are sealed").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public int getSpirituality() {
        return this.requiredSpirituality;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && checkIfCanUseAbility(player) && abilitiesArentSealed(player, player.getItemInHand(hand).getItem())) {
            InteractionResult interactionResult = useAbility(level, player, hand);
            return new InteractionResultHolder<>(interactionResult, player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        boolean x = true;
        if (context.getPlayer() != null && !checkIfCanUseAbility(context.getPlayer()) && abilitiesArentSealed(context.getPlayer(), context.getItemInHand().getItem())) {
            x = false;
        }
        if (!level.isClientSide() && x) {
            return useAbilityOnBlock(context);
        }
        return InteractionResult.PASS;
    }


    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity livingEntity, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!livingEntity.level().isClientSide() && checkIfCanUseAbility(livingEntity) && abilitiesArentSealed(livingEntity, stack.getItem())) {
            return interactLivingEntityLivingEntity(stack, livingEntity, interactionTarget, usedHand);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntityLivingEntity(ItemStack pStack, LivingEntity pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        return InteractionResult.PASS;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(getSpiritualityUsedText(this.requiredSpirituality));
        tooltipComponents.add(getCooldownText(this.cooldown));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    public static Component getSpiritualityUsedText(int requiredSpirituality) {
        return Component.literal("Spirituality Used: ").append(Component.literal(String.valueOf(requiredSpirituality)).withStyle(ChatFormatting.YELLOW));
    }

    public static Component getCooldownText(int cooldown) {
        return Component.literal("Cooldown: ").append(Component.literal(getTextForTicks(cooldown)).withStyle(ChatFormatting.YELLOW));
    }

    public static Component getPathwayText(BeyonderClass beyonderClass) {
        return Component.literal("Pathway: ").append(Component.literal(beyonderClass.sequenceNames().get(9)).withStyle(beyonderClass.getColorFormatting()));
    }

    public static Component getClassText(int requiredSequence, BeyonderClass beyonderClass) {
        return Component.literal("Sequence: ").append(Component.literal(requiredSequence + " - " + beyonderClass.sequenceNames().get(requiredSequence))
                .withStyle(beyonderClass.getColorFormatting()));
    }

    public static void addCooldown(LivingEntity livingEntity, Item item, int cooldown) {
        if (livingEntity instanceof Player player && player.isCreative()) {
            player.getCooldowns().addCooldown(item, 5);
            return;
        }

        if (livingEntity instanceof Player player) {
            if (player.getPersistentData().getBoolean("doorBlinkState")) {
                int distance = player.getPersistentData().getInt("doorBlinkStateDistance");
                int wormsToBeUsed = distance / 10;
                if (distance > 0) {
                    if (distance >= cooldown) {
                        player.getPersistentData().putInt("doorBlinkStateDistance", (int) Math.max(0, distance - cooldown * 1.5f));
                        cooldown = 0;
                    } else {
                        player.getPersistentData().putInt("doorBlinkStateDistance", 0);
                        cooldown = cooldown - distance;
                    }
                    CompoundTag tag = player.getPersistentData();
                    int currentWormOfStar = tag.getInt("wormOfStar");
                    tag.putInt("wormOfStar", Math.max(0, currentWormOfStar - wormsToBeUsed));
                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }
                }
            }
            if (player.getPersistentData().getBoolean("wormOfStarChoice")) {
                CompoundTag tag = player.getPersistentData();
                int wormOfStarAmount = tag.getInt("wormOfStar");
                if (wormOfStarAmount == 0) {
                    player.getCooldowns().addCooldown(item, cooldown);
                } else {
                    int maxReduction = cooldown / 2;
                    int actualReduction = Math.min(maxReduction * (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SEPARATE_WORM_OF_STAR.get()), wormOfStarAmount);
                    int newCooldown = cooldown - actualReduction;
                    tag.putInt("wormOfStar", wormOfStarAmount - actualReduction);
                    if (player instanceof ServerPlayer serverPlayer) {
                        LOTMNetworkHandler.sendToPlayer(new ClientWormOfStarDataS2C(tag.getInt("wormOfStar")), serverPlayer);
                    }
                    player.getCooldowns().addCooldown(item, newCooldown);
                }
            } else {
                player.getCooldowns().addCooldown(item, cooldown);
            }
        } else {
            if (livingEntity.getPersistentData().getBoolean("doorBlinkState")) {
                int distance = livingEntity.getPersistentData().getInt("doorBlinkStateDistance");
                int wormsToBeUsed = distance / 10;
                if (distance > 0) {
                    if (distance >= cooldown) {
                        livingEntity.getPersistentData().putInt("doorBlinkStateDistance", (int) Math.max(0, distance - cooldown * 1.5f));
                        cooldown = 0;
                    } else {
                        livingEntity.getPersistentData().putInt("doorBlinkStateDistance", 0);
                        cooldown = cooldown - distance;
                    }
                    CompoundTag tag = livingEntity.getPersistentData();
                    int currentWormOfStar = tag.getInt("wormOfStar");
                    tag.putInt("wormOfStar", Math.max(0, currentWormOfStar - wormsToBeUsed));
                }
            }
            if (livingEntity.getPersistentData().getBoolean("wormOfStarChoice")) {
                CompoundTag tag = livingEntity.getPersistentData();
                int wormOfStarAmount = tag.getInt("wormOfStar");
                if (wormOfStarAmount == 0) {
                    livingEntity.getPersistentData().putInt("abilityCooldownFor" + item.getDescription().getString(), cooldown);
                } else {
                    int maxReduction = cooldown / 2;
                    int actualReduction = Math.min(maxReduction, wormOfStarAmount);
                    int newCooldown = cooldown - actualReduction;
                    tag.putInt("wormOfStar", wormOfStarAmount - actualReduction);
                    livingEntity.getPersistentData().putInt("abilityCooldownFor" + item.getDescription().getString(), newCooldown);
                }
            } else {
                livingEntity.getPersistentData().putInt("abilityCooldownFor" + item.getDescription().getString(), cooldown);
            }
        }
    }

    public static void removeCooldown(LivingEntity livingEntity, Item item, int newCooldown) {
        if (livingEntity instanceof Player player) {
            player.getCooldowns().removeCooldown(item);
            addCooldown(livingEntity, item, 100);
        } else {
            CompoundTag tag = livingEntity.getPersistentData();
            String cooldownKey = "abilityCooldownFor" + item.getDescription().getString();
            int currentCooldown = tag.getInt(cooldownKey);
            if (currentCooldown > 0) {
                if (newCooldown > 0) {
                    tag.putInt(cooldownKey, newCooldown);
                } else {
                    tag.remove(cooldownKey);
                }
            }
        }
    }

    private static int getCooldownDuration(Item item) {
        if (item instanceof SimpleAbilityItem simpleAbilityItem) {
            return simpleAbilityItem.getCooldown();
        }
        return 100;
    }

    public void addCooldown(LivingEntity player) {
        addCooldown(player, this, this.cooldown);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    protected void baseHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private static String getTextForTicks(int ticks) {
        int min = ticks / 1200;
        double sec = (double) (ticks % 1200) / 20;
        StringBuilder stringBuilder = new StringBuilder();
        if (min > 0) {
            stringBuilder.append(min).append(" minute");
            if (min != 1) {
                stringBuilder.append("s");
            }
            if (sec > 0) {
                stringBuilder.append(" ");
            }
        }
        if (sec > 0) {
            stringBuilder.append(sec).append(" second");
            if (sec != 1) {
                stringBuilder.append("s");
            }
        }
        return stringBuilder.toString();
    }

    public static boolean checkRequiredClass(LivingEntity living, BeyonderClass requiredClass, boolean message) {
        if (!BeyonderUtil.currentPathwayMatchesNoException(living, requiredClass)) {
            String name = requiredClass.sequenceNames().get(9);
            if (message && living instanceof Player player) {
                player.displayClientMessage(
                        Component.literal("You are not of the ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(name).withStyle(requiredClass.getColorFormatting())).append(
                                Component.literal(" Pathway").withStyle(ChatFormatting.AQUA)), true);
            }
            return false;
        }
        return true;
    }

    public static boolean checkRequiredSequence(LivingEntity living, int requiredSequence, boolean message) {
        int sequence = BeyonderUtil.getSequence(living);
        if (sequence > requiredSequence) {
            if (message && living instanceof Player player)
                player.displayClientMessage(
                        Component.literal("You need to be sequence ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(String.valueOf(requiredSequence)).withStyle(ChatFormatting.YELLOW)).append(
                                Component.literal(" or lower to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }

    public static boolean checkSpirituality(LivingEntity living, int requiredSpirituality, boolean message) {
        int spirituality = BeyonderUtil.getSpirituality(living);
        if (spirituality < requiredSpirituality) {
            if (message && living instanceof Player player)
                player.displayClientMessage(
                        Component.literal("You need ").withStyle(ChatFormatting.AQUA).append(
                                Component.literal(String.valueOf(requiredSpirituality)).withStyle(ChatFormatting.YELLOW)).append(
                                Component.literal(" spirituality to use this").withStyle(ChatFormatting.AQUA)), true);
            return false;
        }
        return true;
    }


    public static boolean checkAll(LivingEntity living, BeyonderClass requiredClass, int requiredSequence, int requiredSpirituality, boolean message) {
        Item abilityItem = living.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        boolean isNotSealed = true;
        if(abilityItem instanceof SimpleAbilityItem ability) isNotSealed = ability.abilitiesArentSealed(living, ability);
        return checkRequiredClass(living, requiredClass, message) && checkRequiredSequence(living, requiredSequence, message) && checkSpirituality(living, requiredSpirituality, message) && isNotSealed;
    }


    public static void useSpirituality(LivingEntity livingEntity, int spirituality) {
        BeyonderUtil.useSpirituality(livingEntity, spirituality);
    }

    public boolean useSpirituality(LivingEntity living) {
        if (BeyonderUtil.getSpirituality(living) >= getRequiredSpirituality()) {
            useSpirituality(living, requiredSpirituality);
            return true;
        } else {
            return false;
        }
    }

    public int getRequiredSequence() {
        return this.requiredSequence;
    }

    public int getRequiredSpirituality() {
        return this.requiredSpirituality;
    }


    public BeyonderClass getRequiredPathway() {
        return this.requiredClass.get();
    }


    public boolean checkIfCanUseAbility(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player && player.getCooldowns().isOnCooldown(this)) {
                return false;
            } else {
                if (livingEntity.getPersistentData().getInt("abilityCooldownFor" + this.getDescription().getString()) >= 1) {
                    return false;
                }
            }
            boolean shouldntActiveCalamity = true;
            boolean allowBeyonderAbilitiesNearSpawn = livingEntity.level().getGameRules().getBoolean(GameRuleInit.SHOULD_BEYONDER_ABILITY_NEAR_SPAWN);
            if (!allowBeyonderAbilitiesNearSpawn) {
                BlockPos entityPos = livingEntity.getOnPos();
                BlockPos worldSpawnPos = livingEntity.level().getSharedSpawnPos();
                if (entityPos.closerThan(worldSpawnPos, 300)) {
                    shouldntActiveCalamity = false;
                }
            }
            if (!shouldntActiveCalamity) {
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You are unable to use abilities too close to spawn").withStyle(ChatFormatting.RED), true);
                }
                return false;
            }
            MisfortuneManipulation.livingUseAbilityMisfortuneManipulation(livingEntity);
            CompoundTag tag = livingEntity.getPersistentData();
            if (livingEntity.getMainHandItem().getItem() instanceof SimpleAbilityItem) {
                if (BeyonderUtil.hasStun(livingEntity)) {
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("You are stunned and unable to use abilities for another " + (int) (livingEntity.getPersistentData().getInt("LOTMStun") / 20) + " seconds.").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("cantUseAbility") >= 1) {
                    tag.putInt("cantUseAbility", tag.getInt("cantUseAbility") - 1);
                    if (livingEntity instanceof Player) {
                        livingEntity.sendSystemMessage(Component.literal("How unlucky! You messed up and couldn't use your ability!").withStyle(ChatFormatting.RED));
                    }
                    return false;
                } else if (tag.getInt("unableToUseAbility") >= 1) {
                    tag.putInt("unableToUseAbility", tag.getInt("unableToUseAbility") - 1);
                    if (livingEntity instanceof Player player) {
                        player.displayClientMessage(Component.literal("You are unable to use your ability").withStyle(ChatFormatting.RED), true);
                    }
                }
            }
        }
        return true;
    }


    public interface scribeAbilitiesStorage {
        Map<Item, Integer> getScribedAbilities();

        void copyScribeAbility(Item ability);

        boolean hasScribedAbility(Item ability);

        void useScribeAbility(Item ability);

        int getRemainUses(Item ability);

        int getScribedAbilitiesCount();
    }
}
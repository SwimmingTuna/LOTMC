package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TrickElectricShock extends LeftClickHandlerSkillP {


    public TrickElectricShock(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 50, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        enableDisableElectricShock(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableDisableElectricShock(LivingEntity player) {
        if (!player.level().isClientSide()) {
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(player);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                int amountToStun = (int) (float) BeyonderUtil.getDamage(dimensionalSightTileEntity.getScryTarget()).get(ItemInit.TRICKELECTRICSHOCK.get());
                player.sendSystemMessage(Component.literal("You shocked and stunned your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                BeyonderUtil.applyStun(dimensionalSightTileEntity.getScryTarget(), amountToStun * 4);
            } else {
                CompoundTag tag = player.getPersistentData();
                boolean electricShock = tag.getBoolean("trickmasterElectricShock");
                tag.putBoolean("trickmasterElectricShock", !electricShock);
                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Electric Shock Turned " + (electricShock ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), true);
                }
            }
        }
    }

    public static void trickMasterShockPassive(AttackEntityEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int sequence = BeyonderUtil.getSequence(livingEntity);
        if (event.getTarget() instanceof LivingEntity livingTarget) {
            boolean electricShock = tag.getBoolean("trickmasterElectricShock");
            if (electricShock) {
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    float bbHeight = livingTarget.getBbHeight();
                    float bbWidth = livingTarget.getBbWidth();
                    int particleCount = (int) Math.max(15, (bbWidth + bbHeight) * 3);
                    double targetX = livingTarget.getX();
                    double targetY = livingTarget.getY();
                    double targetZ = livingTarget.getZ();
                    Random random = new Random();
                    for (int i = 0; i < particleCount; i++) {
                        double offsetX = (random.nextDouble() * 2 - 1) * (bbWidth / 2);
                        double offsetY = random.nextDouble() * bbHeight;
                        double offsetZ = (random.nextDouble() * 2 - 1) * (bbWidth / 2);
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, targetX + offsetX, targetY + offsetY, targetZ + offsetZ, 0, 0.0, 0.0, 0.0, 0.0);
                    }
                    int amount = 20 - (sequence * 2);
                    int amountToStun = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKELECTRICSHOCK.get());
                    BeyonderUtil.useSpirituality(livingEntity, amount);
                    BeyonderUtil.applyAwe(livingTarget, amountToStun);
                }
            }
        }
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

        //reach should be___
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 12, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enables or disables your electric shock. If enabled, all of your attacks will very briefly stun at the cost of some spirituality"));
        tooltipComponents.add(Component.literal("Left click for Trick: Escape Trick"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("20 or less per attack while enabled, depending on your sequence.").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (PlayerMobEntity.isCopy(livingEntity)) {
            return 0;
        }
        if (!livingEntity.getPersistentData().getBoolean("trickmasterElectricShock")) {
            return 30;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKESCAPETRICK.get()));
    }
}
package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.blocks.DimensionalSight.DimensionalSightTileEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClientFogDataS2C;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickFog extends LeftClickHandlerSkillP {


    public TrickFog(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 50, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        createFog(player);
        return InteractionResult.SUCCESS;
    }

    public static void createFog(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFOG.get());
            DimensionalSightTileEntity dimensionalSightTileEntity = BeyonderUtil.findNearbyDimensionalSight(livingEntity);
            if (dimensionalSightTileEntity != null && dimensionalSightTileEntity.getScryTarget() != null) {
                livingEntity.sendSystemMessage(Component.literal("You created fog around your Dimensional Sight Target").withStyle(ChatFormatting.AQUA));
                for (LivingEntity living : BeyonderUtil.checkEntitiesInLocation(livingEntity, (float) (damage * 2), (float) dimensionalSightTileEntity.getScryTarget().getX(), (float) dimensionalSightTileEntity.getScryTarget().getY(), (float) dimensionalSightTileEntity.getScryTarget().getZ())) {
                    if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                        if (living instanceof ServerPlayer serverPlayer) {
                            LOTMNetworkHandler.sendToPlayer(new ClientFogDataS2C(damage * 6), serverPlayer);
                        } else if (living instanceof Mob mob && mob.getTarget() != null) {
                            if (mob.distanceTo(mob.getTarget()) > 10) {
                                mob.setTarget(null);
                            }
                        }
                    }
                }
            } else {
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(damage * 2))) {
                    if (living != livingEntity && !BeyonderUtil.areAllies(livingEntity, living)) {
                        if (living instanceof ServerPlayer serverPlayer) {
                            LOTMNetworkHandler.sendToPlayer(new ClientFogDataS2C(damage * 6), serverPlayer);
                        } else if (living instanceof Mob mob && mob.getTarget() != null) {
                            if (mob.distanceTo(mob.getTarget()) > 10) {
                                mob.setTarget(null);
                            }
                        }
                    }
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
        tooltipComponents.add(Component.literal("Upon use, all non-allies will be affected by a fog that will reduce their vision greatly, also causing mobs to lose their target if they're far away."));
        tooltipComponents.add(Component.literal("Left click for Trick: Freezing"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("50").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
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
        if (target instanceof Player) {
            int damage = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKFOG.get());
            if (target.distanceTo(livingEntity) < damage) {
                return 40;
            }
            return 0;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKFREEZING.get()));
    }
}
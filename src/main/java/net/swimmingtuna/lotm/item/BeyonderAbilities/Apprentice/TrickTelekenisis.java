package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.packet.UpdateItemInHandC2S;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EFunctions;
import net.swimmingtuna.lotm.nihilums.tweaks.EventManager.EventManager;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickHandlerSkillP;
import net.swimmingtuna.lotm.nihilums.tweaks.LeftClickHandler.LeftClickType;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TrickTelekenisis extends LeftClickHandlerSkillP {


    public TrickTelekenisis(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        useSpirituality(player);
        addCooldown(player);
        enableDisableTelekenesis(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableDisableTelekenesis(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean telekenisis = tag.getBoolean("trickmasterTelekenisis");
            tag.putBoolean("trickmasterTelekenisis", !telekenisis);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Telekenisis Turned " + (telekenisis ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            }
            if (player.getPersistentData().getBoolean("trickmasterTelekenisis")) {
                EventManager.addToRegularLoop(player, EFunctions.TRICKMASTERTELEKENESIS.get());
            } else {
                EventManager.removeFromRegularLoop(player, EFunctions.TRICKMASTERTELEKENESIS.get());
            }
        }
    }


    public static void trickMasterTelekenisisPassive(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && tag.getBoolean("trickmasterTelekenisis") && livingEntity.tickCount % 5 == 0) {
            if (BeyonderUtil.getSpirituality(livingEntity) >= 10) {
                for (Entity entity : livingEntity.level().getEntitiesOfClass(Entity.class, livingEntity.getBoundingBox().inflate(BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKTELEKENISIS.get())))) {
                    if (entity == livingEntity) {
                        continue;
                    }
                    if (!BeyonderUtil.isEntityAlly(livingEntity, entity)) {
                        double x = entity.getX() - livingEntity.getX();
                        double y = entity.getY() - livingEntity.getY();
                        double z = entity.getZ() - livingEntity.getZ();
                        double magnitude = Math.sqrt(x * x + y * y + z * z);
                        entity.setDeltaMovement(x / magnitude * 4, y / magnitude * 4, z / magnitude * 4);
                        entity.hurtMarked = true;
                        float amount;
                        if (entity instanceof LivingEntity living) {
                            amount = (10 - BeyonderUtil.getSequence(living)) * 10;
                        } else if (entity instanceof Projectile projectile) {
                            amount = (int) ((BeyonderUtil.getScale(entity) * 3) + (Math.abs(projectile.getDeltaMovement().y() + projectile.getDeltaMovement().x() + projectile.getDeltaMovement().z())) * 3);
                        } else {
                            amount = 0;
                        }
                        BeyonderUtil.useSpirituality(livingEntity, (int) amount);
                    }
                }
            } else {
                EventManager.removeFromRegularLoop(livingEntity, EFunctions.TRICKMASTERTELEKENESIS.get());
                livingEntity.getPersistentData().putBoolean("trickmasterTelekenisis", false);
                if (livingEntity instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Telekenisis turned off due to lack of spirituality").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), true);
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
        tooltipComponents.add(Component.literal("Upon use, enables or disables your telekenisis. If enabled, all projectiles and other entities will be pushed away from you at the cost of spirituality for each entity pushed away."));
        tooltipComponents.add(Component.literal("Left click for Trick: Tumble"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("Depends on strength of entity pushed.").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second.").withStyle(ChatFormatting.YELLOW)));
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
        if (target != null && !livingEntity.getPersistentData().getBoolean("trickmasterTelekenisis")) {
            if (livingEntity.getHealth() < target.getHealth()) {
                return (int) (50 * livingEntity.getHealth() / livingEntity.getMaxHealth());
            }
            return 0;
        } else if (target == null && livingEntity.getPersistentData().getBoolean("trickmasterTelekenisis")) {
            return 60;
        }
        return 0;
    }
    @Override
    public <T> LeftClickType getleftClickEmpty(T item) {
        return new UpdateItemInHandC2S((Integer) item, new ItemStack(ItemInit.TRICKTUMBLE.get()));
    }
}
package net.swimmingtuna.lotm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class AllyMaker extends Item {


    public AllyMaker(Properties pProperties) {
        super(pProperties);
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!pPlayer.level().isClientSide) {
            if (pPlayer.level() instanceof ServerLevel serverLevel) {
                PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
                if (allyData.getAllies(pPlayer.getUUID()).contains(pInteractionTarget.getUUID())) {
                    allyData.removeAlly(pPlayer.getUUID(), pInteractionTarget.getUUID());
                    pPlayer.sendSystemMessage(Component.literal("Removed " + pInteractionTarget.getName().getString() + " as an ally!").withStyle(ChatFormatting.RED));
                } else {
                    boolean isNonHostile = !(pInteractionTarget instanceof Enemy);
                    if (isNonHostile) {
                        allyData.addAlly(pInteractionTarget.getUUID(), pPlayer.getUUID());
                    }
                    allyData.addAlly(pPlayer.getUUID(), pInteractionTarget.getUUID());
                    pPlayer.sendSystemMessage(Component.literal("Added " + pInteractionTarget.getName().getString() + " as an ally!").withStyle(ChatFormatting.GREEN));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 300, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 300, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Use on an entity to ally with them.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.literal("To be considered allies as a player, both players must ally each other.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("If used on a friendly mob, then it automatically adds you to the mob's allies as well.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.literal("You can also use the /ally command for this and more.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.LIGHT_PURPLE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}


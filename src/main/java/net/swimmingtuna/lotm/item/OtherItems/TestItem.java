package net.swimmingtuna.lotm.item.OtherItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.attributes.AttributeHelper;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;

public class TestItem extends SimpleAbilityItem {


    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public TestItem(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 3, 600, 40);
    }


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
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 200, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext pContext) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {


        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity livingEntity, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (livingEntity instanceof Player player) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty()) {
                        player.getCooldowns().removeCooldown(stack.getItem());
                    }
                }
            }
            BeyonderUtil.setGray(livingEntity, 100);
            if (livingEntity.isShiftKeyDown()) {
                BeyonderUtil.setGray(livingEntity, 0);
            }
            /*
            MinecraftServer server = livingEntity.getServer();
            if (server != null && livingEntity instanceof Player pPlayer) {
                if (pPlayer.level().dimension() == Level.OVERWORLD) {
                    ServerLevel spiritWorld = server.getLevel(DimensionInit.SPIRIT_WORLD_LEVEL_KEY);
                    if (BeyonderUtil.getSequence(pPlayer) == 0) {
                        if (spiritWorld != null) {
                            pPlayer.sendSystemMessage(Component.literal("Transporting to Spirit World...").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.BLUE));
                            pPlayer.teleportTo(spiritWorld, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), Set.of(), pPlayer.getYRot(), pPlayer.getXRot());
                        }
                    }
                } else if (pPlayer.level().dimension() == DimensionInit.SPIRIT_WORLD_LEVEL_KEY) {
                    ServerLevel overworldWorld = server.getLevel(Level.OVERWORLD);
                    if (overworldWorld != null) {
                        pPlayer.sendSystemMessage(Component.literal("Transporting to Overworld...").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN));
                        pPlayer.teleportTo(overworldWorld, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), Set.of(), pPlayer.getYRot(), pPlayer.getXRot());
                    }
                }
            }

             */
        }
        return InteractionResult.SUCCESS;
    }
}

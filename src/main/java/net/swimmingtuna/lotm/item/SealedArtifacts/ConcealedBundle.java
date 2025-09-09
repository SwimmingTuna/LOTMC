package net.swimmingtuna.lotm.item.SealedArtifacts;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.screen.ConcealedBundleMenu;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class ConcealedBundle extends Item {
    public ConcealedBundle(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            if(hand == InteractionHand.OFF_HAND && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) return InteractionResultHolder.fail(stack);
            CompoundTag tag = stack.getOrCreateTag();
            if(tag.getInt("concealedBundleUses") <= tag.getInt("concealedBundleMaxDurability") || tag.getBoolean("concealedBundleUnbreakable")) {
                if(!tag.getBoolean("concealedBundleUnbreakable")) tag.putInt("concealedBundleUses", tag.getInt("concealedBundleUses") + 1);
                int slot = getSlotForHand(player, hand);
                NetworkHooks.openScreen(
                        (ServerPlayer) player,
                        new SimpleMenuProvider(
                                (windowId, inv, p) -> new ConcealedBundleMenu(windowId, inv, stack, slot, tag.getInt("concealedBundleRows")),
                                Component.literal(stack.getDisplayName().getString()).withStyle(ChatFormatting.WHITE)
                        ),
                        buffer -> {
                            buffer.writeItem(stack);
                            buffer.writeInt(slot);
                            buffer.writeInt(tag.getInt("concealedBundleRows"));
                        }
                );
            } else {
                if(player.getItemInHand(hand).getItem() instanceof ConcealedBundle &&
                        player.getItemInHand(hand).getOrCreateTag().getInt("concealedBundleUses") > tag.getInt("concealedBundleMaxDurability")) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                    player.displayClientMessage(Component.literal("Your concealed bag ran out of uses. All the items stored are forever lost in the Spirit World.").withStyle(BeyonderUtil.getStyle(player)), true);
                    return InteractionResultHolder.sidedSuccess(ItemStack.EMPTY, world.isClientSide());
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (!world.isClientSide && entity instanceof Player player) {
            if(isSelected) {
                if (BeyonderUtil.currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.APPRENTICE.get(), 4)) {
                    if(player.isShiftKeyDown()) {
                        CompoundTag tag = stack.getOrCreateTag();
                        if(!tag.getBoolean("concealedBundleUnbreakable")) player.displayClientMessage((Component.literal("Concealed Bag remaining uses: " + (stack.getOrCreateTag().getInt("concealedBundleMaxDurability") - stack.getOrCreateTag().getInt("concealedBundleUses"))).withStyle(BeyonderUtil.getStyle(player))), true);
                        else player.displayClientMessage(Component.literal("The Concealed Bag connection with the Spirit World is perfectly tight, and it doesn't seem it's going to diminish anytime soon. ").withStyle(BeyonderUtil.getStyle(player)), true);
                    }
                }
            }
        }
    }

    int getSlotForHand(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return player.getInventory().selected;
        } else {
            return 40;
        }
    }
}

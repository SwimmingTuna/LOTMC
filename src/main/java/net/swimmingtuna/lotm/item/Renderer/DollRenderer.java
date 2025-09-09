package net.swimmingtuna.lotm.item.Renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.OtherItems.Doll;
import net.swimmingtuna.lotm.util.IItemRenderer;
import net.swimmingtuna.lotm.util.PerspectiveModelState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DollRenderer implements IItemRenderer {
    private static final Set<Entity> brokenMobs = new HashSet<>();
    public static Map<UUID, PlayerMobEntity> playerRendererBuffer = new HashMap<>();

    private float TARGET_HEIGHT = 0.25F; // 1/4 block

    public DollRenderer() {}

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack mStack, MultiBufferSource getter, int packedLight, int packedOverlay) {
        Entity mob;
        if (stack.hasTag() && stack.getOrCreateTag().contains("EntityPlayerName") &&  stack.getOrCreateTag().contains("EntityPlayerUUID")) {
            if(!playerRendererBuffer.containsKey(stack.getOrCreateTag().getUUID("EntityPlayerUUID"))){
                PlayerMobEntity clone = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), Minecraft.getInstance().level);
                clone.getPersistentData().putBoolean("is_sitting", true);
                GameProfile gameProfile = new GameProfile(stack.getTag().getUUID("EntityPlayerUUID"), stack.getTag().getString("EntityPlayerName"));
                clone.setIsClone(true);
                clone.setUsername(gameProfile.getName());
                clone.setProfile(gameProfile);
                Minecraft.getInstance().getSkinManager().registerSkins(gameProfile, clone.getSkinCallback(), true);
                ListTag itemListTag = stack.getTag().getList("EntityPlayerEquipment", Tag.TAG_COMPOUND);
                List<ItemStack> itemStacks = new ArrayList<>();

                for (int i = 0; i < itemListTag.size(); i++) {
                    CompoundTag stackTag = itemListTag.getCompound(i);
                    ItemStack equipments = ItemStack.of(stackTag);
                    itemStacks.add(equipments);
                }
                if(itemStacks.size() == 6) {
                    clone.setItemSlot(EquipmentSlot.MAINHAND, itemStacks.get(0));
                    clone.setItemSlot(EquipmentSlot.OFFHAND, itemStacks.get(1));
                    clone.setItemSlot(EquipmentSlot.HEAD, itemStacks.get(2));
                    clone.setItemSlot(EquipmentSlot.CHEST, itemStacks.get(3));
                    clone.setItemSlot(EquipmentSlot.LEGS, itemStacks.get(4));
                    clone.setItemSlot(EquipmentSlot.FEET, itemStacks.get(5));
                }
                playerRendererBuffer.put(stack.getOrCreateTag().getUUID("EntityPlayerUUID"), clone);
                mob = clone;
            }else{
                mob = playerRendererBuffer.get(stack.getOrCreateTag().getUUID("EntityPlayerUUID"));
            }
        } else {
            mob = Doll.getRenderEntityStatic(stack);
        }
        if (mob == null || brokenMobs.contains(mob)) return;
        if (mob instanceof PlayerMobEntity) this.TARGET_HEIGHT = 0.5f;
        mob.tickCount = 0;
        if(transformType == ItemDisplayContext.GUI){
            float scale = 1F / Math.max(mob.getBbWidth(), mob.getBbHeight());
            mStack.translate(0.5, 0, 0.5);
            mStack.scale(scale, scale, scale);

            EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
            manager.render(mob, 0, 0, 0, 0, 0, mStack, getter, packedLight);
        }else {
            float entityHeight = mob.getBbHeight();
            if (entityHeight <= 0.01f) return;

            float scale = TARGET_HEIGHT / entityHeight;
            mStack.translate(0.5, 0.5, 0.6);
            mStack.mulPose(Axis.YP.rotationDegrees(180));
            mStack.scale(scale, scale, scale);

            EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
            manager.render(mob, 0, 0, 0, 0, 0, mStack, getter, packedLight);
        }
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return PerspectiveModelState.IDENTITY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
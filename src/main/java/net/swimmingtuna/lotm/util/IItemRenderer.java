package net.swimmingtuna.lotm.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.List;

public interface IItemRenderer extends PerspectiveModel{
    void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay);

    //Useless methods for IItemRenderer.
    //@formatter:off
    @Override default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) { return Collections.emptyList(); }
    @Override default boolean isCustomRenderer() { return true; }
    @Override default TextureAtlasSprite getParticleIcon() { return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation()); }
    @Override default ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
    //@formatter:on
}

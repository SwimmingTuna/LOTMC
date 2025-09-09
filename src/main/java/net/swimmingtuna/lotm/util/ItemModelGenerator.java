package net.swimmingtuna.lotm.util;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.init.ItemInit;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), LOTM.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        dummyItem(ItemInit.DOLL);
    }

    private void dummyItem(RegistryObject<? extends Item> item) {
        getBuilder(item.getId().getPath())//
                .parent(new ModelFile.UncheckedModelFile("builtin/generated"));
    }
}

package net.swimmingtuna.lotm.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ClassModelLoader implements IGeometryLoader<ClassModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        try {
            // Load the class name from JSON
            String className = json.get("class").getAsString();
            Class<?> clazz = Class.forName(className);

            // Ensure it has a public no-arg constructor
            Constructor<? extends BakedModel> ctor = clazz.asSubclass(BakedModel.class).getConstructor();

            return new Geometry(ctor);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new JsonParseException("Failed to load BakedModel class.", e);
        }
    }

    public record Geometry(Constructor<? extends BakedModel> ctor) implements IUnbakedGeometry<Geometry> {
        @Override
        public BakedModel bake(
                IGeometryBakingContext context,
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelState,
                ItemOverrides overrides,
                ResourceLocation modelLocation
        ) {
            try {
                return ctor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to instantiate BakedModel.", e);
            }
        }
    }
}

package net.swimmingtuna.lotm.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.world.worldgen.ConcealedSpaceChunkGenerator;
import net.swimmingtuna.lotm.world.worldgen.DollSpaceChunkGenerator;

public class ChunkGeneratorInit {

    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, LOTM.MOD_ID);

    public static final RegistryObject<Codec<? extends ChunkGenerator>> CONCEALED_SPACE =
            CHUNK_GENERATORS.register("concealed_space", () -> ConcealedSpaceChunkGenerator.CODEC);
    public static final RegistryObject<Codec<? extends ChunkGenerator>> DOLL_SPACE =
            CHUNK_GENERATORS.register("doll_space", () -> DollSpaceChunkGenerator.CODEC);

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
    }

}
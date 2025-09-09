package net.swimmingtuna.lotm.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class DoorParticle extends TextureSheetParticle {
    protected DoorParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSize(0.2F, 0.2F);
        this.lifetime = 20;
        this.gravity = 0.0F;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(@NotNull SimpleParticleType particleType, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            DoorParticle doorParticle = new DoorParticle(level, x, y, z, dx, dy, dz, this.spriteSet);
            doorParticle.setColor(1F, 1F, 1F);
            return doorParticle;
        }
    }
}

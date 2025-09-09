package net.swimmingtuna.lotm.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class BlackCurtainParticle extends TextureSheetParticle {
    protected BlackCurtainParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.lifetime = 400;
        this.gravity = 0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.setSize(0.3F, 0.3F);
        this.pickSprite(spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
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
            BlackCurtainParticle blackCurtainParticle = new BlackCurtainParticle(level, x, y, z, this.spriteSet, dx, dy, dz);
            blackCurtainParticle.setColor(1F, 1F, 1F);
            return blackCurtainParticle;
        }
    }
}
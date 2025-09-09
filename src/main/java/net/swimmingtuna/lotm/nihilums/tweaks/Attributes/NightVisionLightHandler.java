package net.swimmingtuna.lotm.nihilums.tweaks.Attributes;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NightVisionLightHandler {
    static final int raysAmount = 20; //should be the same as yaws/pitches amount
    //static final float[] yaws = {0f, 5f, -5f, 0f, 0f};
    // static final float[] pitches = {0f, 0f, 0f, 5f, -5f};
    static final int maxDistance = 50;
    static int iteration = 0;

    private static Vec3 getDirectionInCone(Vec3 forward, float spreadDegrees){
        RandomSource random = RandomSource.create();
        float spreadRad = (float)Math.toRadians(spreadDegrees);
        float yaw = (random.nextFloat() - 0.5f)  * spreadRad;
        float pitch = (random.nextFloat() - 0.5f)  * spreadRad;

        // float yaw = yaws[iteration]  * spreadRad;
        //float pitch = pitches[iteration] * spreadRad;

        Vec3 rotated = forward
                .yRot(yaw)
                .xRot(pitch);

        iteration++;
        if(iteration == raysAmount)
            iteration = 0;

        return rotated.normalize();
    }

    public static float getLigthLevelInFov(Level level, Player player){
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 end = player.getLookAngle().normalize().scale(maxDistance);

        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        if(hitResult.getType() == HitResult.Type.BLOCK)
            return level.getRawBrightness(hitResult.getBlockPos(), 0);
        else
            return level.getRawBrightness(player.blockPosition(), 0);
    }

    public static float getAverageLightInFOV(Level level, Player player, float spreadDegrees) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle().normalize();

        int totalLight = 0;
        int hits = 0;

        for (int i = 0; i < raysAmount; i++) {

            Vec3 rayDir = getDirectionInCone(lookVec, spreadDegrees);
            Vec3 end = eyePos.add(rayDir.scale(maxDistance));

            BlockHitResult hitResult = level.clip(new ClipContext(
                    eyePos, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                int light = level.getLightEngine().getRawBrightness(pos, 0);
                totalLight += light;
                hits++;
            }
        }

        return hits == 0 ? 0 : (float) totalLight / hits;
    }

    public static boolean checkDay(Level world){
        if(world.isDay()){
            long time = world.getDayTime() % 24000L;
            return (time >= 0 && time < 13000) || time >= 22500;
        }
        return false;
    }
}

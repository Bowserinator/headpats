package net.hellomouse;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class PatUtil {
    public static final double MAX_HEADPAT_DISTANCE = 1.5;

    public static boolean canHeadpatInteract(Vec3d hitPos, PlayerEntity player, PlayerEntity targetPlayer) {
        double y = hitPos.y / (targetPlayer.getScaleFactor());
        double height = targetPlayer.getHeight() / (targetPlayer.getScaleFactor());
        return y > height - 0.5
                && player.getMainHandStack().isEmpty()
                && targetPlayer.squaredDistanceTo(player) < MAX_HEADPAT_DISTANCE * MAX_HEADPAT_DISTANCE;
    }

    public static boolean canHeadpatServer(PlayerEntity player, PlayerEntity target) {
        return player.squaredDistanceTo(target) < MAX_HEADPAT_DISTANCE * MAX_HEADPAT_DISTANCE * 9;
    }
}

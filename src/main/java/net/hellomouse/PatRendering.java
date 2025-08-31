package net.hellomouse;

import net.hellomouse.patstate.PatState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;


import javax.annotation.Nullable;

public class PatRendering {
    public static void modifyHandMatrix(PlayerEntity player, float tickDelta, MatrixStack matrices) {
        var petting = PatState.CLIENT_INSTANCE.get(player);
        if (petting == null)
            return;
        if (petting.pettingMultiplier > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettingTicks, (float) petting.pettingTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettingMultiplier, petting.pettingMultiplier);
            matrices.translate(player.getMainArm() == Arm.RIGHT ? 1 : -1, -1, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(petTime * 0.4f) * 16.0f * multiplier));
            matrices.translate(player.getMainArm() == Arm.RIGHT ? -1 : 1, 1, 0);
            matrices.translate(0, 0.2, 0);
        }
    }

    public static void setPetAngles(PlayerEntity player, ModelPart rightArm, ModelPart leftArm, ModelPart head, ModelPart hat) {
        var pettingState = PatState.CLIENT_INSTANCE.get(player);
        if (pettingState == null) return;
        var pettingTime = pettingState.pettingTicks;
        var pettingMultiplier = pettingState.pettingMultiplier;

        // Patter
        if (pettingMultiplier > 0) {
            var arm = player.getMainArm();
            if (arm == Arm.RIGHT) {
                rightArm.pitch = rightArm.pitch * (1 - pettingMultiplier) - pettingMultiplier * 2.1f;
                rightArm.yaw = rightArm.yaw * (1 - pettingMultiplier) - MathHelper.sin(pettingTime * 0.4f) * pettingMultiplier * 0.5f;
            } else {
                leftArm.pitch = leftArm.pitch * (1 - pettingMultiplier) - pettingMultiplier * 2.1f;
                leftArm.yaw = leftArm.yaw * (1 - pettingMultiplier) - MathHelper.sin(pettingTime * 0.4f) * pettingMultiplier * 0.5f;
            }
        }

        // Pattee
        var pettedTime = pettingState.pettedTicks;
        var pettedMultiplier = pettingState.pettedMultiplier;

        if (pettedMultiplier > 0) {
            head.pitch += pettedMultiplier * 0.4f;
            head.roll = -MathHelper.sin(pettedTime * 0.4f) * pettedMultiplier * 0.15f;
            hat.pitch += pettedMultiplier * 0.4f;
            hat.roll = -MathHelper.sin(pettedTime * 0.4f) * pettedMultiplier * 0.15f;
        } else {
            head.roll = 0;
            hat.roll = 0;
        }
    }

    public static float getCameraRoll(PlayerEntity player, float tickDelta) {
        var petting = PatState.CLIENT_INSTANCE.get(player);
        if (petting == null)
            return 0F;
        var finalFirstPersonSwayStrength = HeadpatMod.CONFIG.firstPersonSwayStrength * MinecraftClient.getInstance().options.getDistortionEffectScale().getValue();

        if (petting.pettedMultiplier > 0 && finalFirstPersonSwayStrength > 0) {
            var petTime = MathHelper.lerp(tickDelta, (float) petting.prevPettedTicks, (float) petting.pettedTicks);
            var multiplier = MathHelper.lerp(tickDelta, petting.prevPettedMultiplier, petting.pettedMultiplier);
            return -MathHelper.sin(petTime * 0.4f) * multiplier * 0.1f * (float) finalFirstPersonSwayStrength;
        }
        return 0F;
    }
}

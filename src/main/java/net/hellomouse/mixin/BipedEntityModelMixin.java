package net.hellomouse.mixin;

import net.hellomouse.PatRendering;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Final
    @Shadow
    public ModelPart head;

    @Final
    @Shadow
    public ModelPart hat;

    @Inject(
        method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
        at = @At(value = "TAIL")
    )
    private void armAnimation(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player)
            PatRendering.setPetAngles(player, leftArm, rightArm, head, hat);
    }
}

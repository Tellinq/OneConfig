package org.polyfrost.oneconfig.internal.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.PostWorldRenderEvent;
import org.polyfrost.universal.UMatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    //#if FABRIC
                    //$$ ordinal = 1
                    //#else
                    //#if MC >= 1.16.5
                    //$$ ordinal = 1
                    //#else
                    ordinal = 2
                    //#endif
                    //#endif
            ),
            slice = @Slice(
                    //#if MC >= 1.16.5
                    //$$ from = @At(
                    //$$     value = "INVOKE",
                    //$$     target = "Lnet/minecraft/client/renderer/WorldRenderer;updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V"
                    //$$ ),
                    //$$ to = @At(
                    //$$     value = "INVOKE",
                    //$$     target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V"
                    //$$ )
                    //#else
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/GlStateManager;disableFog()V"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand(FI)V"
                    )
                    //#endif
            )
    )
    private void onRenderWorldPass(
            //#if MC <= 1.12.2
            int pass,
            //#endif
            float partialTicks,
            long finishTimeNano,
            //#if MC >= 1.16.5
            //$$ com.mojang.blaze3d.matrix.MatrixStack matrixStack,
            //#endif
            CallbackInfo ci
    ) {
        UMatrixStack stack =
                //#if MC >= 1.16.5
                //$$ new UMatrixStack(matrixStack);
                //#else
                new UMatrixStack();
                //#endif
        EventManager.INSTANCE.post(new PostWorldRenderEvent(stack, partialTicks));
    }

}

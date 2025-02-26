package org.polyfrost.oneconfig.internal.mixin.events;

import dev.deftu.omnicore.client.render.OmniMatrixStack;
import net.minecraft.client.renderer.EntityRenderer;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.PostWorldRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class Mixin_PostWorldRenderEvent {

    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"
                    //#if FABRIC
                    //#if MC <= 1.12.2
                    //$$ , ordinal = 1
                    //#endif
                    //#else
                    //#if MC >= 1.16.5
                    //$$ , ordinal = 1
                    //#else
                    , ordinal = 2
                    //#endif
                    //#endif
            ),
            slice = @Slice(
                    //#if MC >= 1.16.5
                    //$$ from = @At(
                    //$$     value = "INVOKE",
                    //#if MC >= 1.17.1
                    //#if FABRIC
                    //#if MC >= 1.19.4
                    //$$     target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;)V"
                    //#else
                    //$$     target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V"
                    //#endif
                    //#else
                    //$$     target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V"
                    //#endif
                    //#else
                    //$$     target = "Lnet/minecraft/client/renderer/WorldRenderer;updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V"
                    //#endif
                    //$$ ),
                    //$$ to = @At(
                    //$$     value = "INVOKE",
                    //#if MC >= 1.17.1
                    //#if FABRIC
                    //$$     target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V"
                    //#else
                    //$$     target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V"
                    //#endif
                    //#else
                    //$$     target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/ActiveRenderInfo;F)V"
                    //#endif
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
            //$$ com.mojang.blaze3d.vertex.PoseStack matrixStack,
            //#endif
            CallbackInfo ci
    ) {
        OmniMatrixStack stack =
                //#if MC >= 1.16.5
                //$$ new OmniMatrixStack(matrixStack);
                //#else
                new OmniMatrixStack();
                //#endif
        EventManager.INSTANCE.post(new PostWorldRenderEvent(stack, partialTicks));
    }

}

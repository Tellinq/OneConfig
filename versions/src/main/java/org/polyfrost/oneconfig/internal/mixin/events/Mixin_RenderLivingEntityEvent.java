package org.polyfrost.oneconfig.internal.mixin.events;

//#if MC >= 1.16.5
//$$ import com.mojang.blaze3d.matrix.MatrixStack;
//$$ import net.minecraft.client.renderer.IRenderTypeBuffer;
//$$ import net.minecraft.client.renderer.entity.LivingRenderer;
//#else
import net.minecraft.client.renderer.entity.RendererLivingEntity;
//#endif

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.profiler.Profiler;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.RenderLivingEntityEvent;
import org.polyfrost.universal.UMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.16.5
//$$ @Mixin(LivingRenderer.class)
//#else
@Mixin(RendererLivingEntity.class)
//#endif
public class Mixin_RenderLivingEntityEvent<T extends EntityLivingBase> {

    @Inject(
            //#if MC >= 1.17.1
            //#if FABRIC
            //$$ method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            //#else
            //$$ method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            //#endif
            //#elseif MC >= 1.16.5
            //$$ method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            //#elseif MC >= 1.12.2
            //$$ method = "doRender(Lnet/minecraft/entity/EntityLiving;DDDFF)V",
            //#else
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            //#endif
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPreEntityRenderCallback(
            T entity,
            //#if MC <= 1.12.2
            double x,
            double y,
            double z,
            //#endif
            float entityYaw,
            float partialTicks,
            //#if MC >= 1.16.5
            //$$ MatrixStack matrixStack,
            //$$ IRenderTypeBuffer buffer,
            //$$ int packedLight,
            //#endif
            CallbackInfo ci
    ) {
        Profiler profiler = UMinecraft.getMinecraft()
                //#if MC >= 1.16.5
                //$$ .getProfiler();
                //#else
                .mcProfiler;
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_pre");

        //#if MC >= 1.16.5
        //$$ double x = entity.getPosX();
        //$$ double y = entity.getPosY();
        //$$ double z = entity.getPosZ();
        //#endif
        RenderLivingEntityEvent event = new RenderLivingEntityEvent.Pre(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        if (event.cancelled) {
            ci.cancel();
        }

        profiler.endSection();
    }

    @Inject(
            //#if MC >= 1.17.1
            //#if FABRIC
            //$$ method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            //#else
            //$$ method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            //#endif
            //#elseif MC >= 1.16.5
            //$$ method = "render(Lnet/minecraft/entity/LivingEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V",
            //#elseif MC >= 1.12.2
            //$$ method = "doRender(Lnet/minecraft/entity/EntityLiving;DDDFF)V",
            //#else
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            //#endif
            at = @At("TAIL")
    )
    private void onPostEntityRenderCallback(
            T entity,
            //#if MC <= 1.12.2
            double x,
            double y,
            double z,
            //#endif
            float entityYaw,
            float partialTicks,
            //#if MC >= 1.16.5
            //$$ MatrixStack matrixStack,
            //$$ IRenderTypeBuffer buffer,
            //$$ int packedLight,
            //#endif
            CallbackInfo ci
    ) {
        Profiler profiler = UMinecraft.getMinecraft()
                //#if MC >= 1.16.5
                //$$ .getProfiler();
                //#else
                .mcProfiler;
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_post");

        //#if MC >= 1.16.5
        //$$ double x = entity.getPosX();
        //$$ double y = entity.getPosY();
        //$$ double z = entity.getPosZ();
        //#endif
        RenderLivingEntityEvent event = new RenderLivingEntityEvent.Post(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        // Can't cancel when the method has already returned lol

        profiler.endSection();
    }

}

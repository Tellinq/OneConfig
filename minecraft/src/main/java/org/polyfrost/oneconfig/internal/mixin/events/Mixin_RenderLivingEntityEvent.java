package org.polyfrost.oneconfig.internal.mixin.events;

import dev.deftu.omnicore.client.OmniClient;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.profiler.Profiler;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.16.5
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//#endif

//#if MC >= 1.21.4
//$$ import net.minecraft.util.profiler.Profilers;
//#endif

@Mixin(RendererLivingEntity.class)
public class Mixin_RenderLivingEntityEvent<T extends EntityLivingBase> {

    @Inject(
            //#if MC >= 1.16.5
            //$$ method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
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
            //$$ PoseStack matrixStack,
            //$$ MultiBufferSource buffer,
            //$$ int packedLight,
            //#endif
            CallbackInfo ci
    ) {
        Profiler profiler =
                //#if MC >= 1.21.4
                //$$ Profilers.get();
                //#else
                OmniClient.getInstance()
                        //#if MC >= 1.16.5
                        //$$ .getProfiler();
                        //#else
                        .mcProfiler;
                        //#endif
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_pre");

        //#if MC >= 1.16.5
        //$$ double x = entity.getX();
        //$$ double y = entity.getY();
        //$$ double z = entity.getZ();
        //#endif
        RenderLivingEvent event = new RenderLivingEvent.Pre(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        if (event.cancelled) {
            ci.cancel();
        }

        profiler.endSection();
    }

    @Inject(
            //#if MC >= 1.16.5
            //$$ method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
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
            //$$ PoseStack matrixStack,
            //$$ MultiBufferSource buffer,
            //$$ int packedLight,
            //#endif
            CallbackInfo ci
    ) {
        Profiler profiler =
                //#if MC >= 1.21.4
                //$$ Profilers.get();
                //#else
                OmniClient.getInstance()
                    //#if MC >= 1.16.5
                    //$$ .getProfiler();
                    //#else
                    .mcProfiler;
                    //#endif
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_post");

        //#if MC >= 1.16.5
        //$$ double x = entity.getX();
        //$$ double y = entity.getY();
        //$$ double z = entity.getZ();
        //#endif
        RenderLivingEvent event = new RenderLivingEvent.Post(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        // Can't cancel when the method has already returned lol

        profiler.endSection();
    }

}

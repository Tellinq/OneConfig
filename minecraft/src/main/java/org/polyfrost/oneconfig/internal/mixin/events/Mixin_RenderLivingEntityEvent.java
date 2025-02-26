package org.polyfrost.oneconfig.internal.mixin.events;

//#if MC >= 1.16.5
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.renderer.MultiBufferSource;
//$$ import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//#else
import net.minecraft.client.renderer.entity.RendererLivingEntity;
//#endif

import dev.deftu.omnicore.client.OmniClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.profiler.Profiler;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.RenderLivingEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class Mixin_RenderLivingEntityEvent<T extends EntityLivingBase> {

    @Inject(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
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
        Profiler profiler = OmniClient.getInstance()
                //#if MC >= 1.16.5
                //$$ .getProfiler();
                //#else
                .mcProfiler;
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_pre");

        //#if MC >= 1.16.5
        //$$ double x = entity.getX();
        //$$ double y = entity.getY();
        //$$ double z = entity.getZ();
        //#endif
        RenderLivingEntityEvent event = new RenderLivingEntityEvent.Pre(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        if (event.cancelled) {
            ci.cancel();
        }

        profiler.endSection();
    }

    @Inject(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
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
        Profiler profiler = OmniClient.getInstance()
                //#if MC >= 1.16.5
                //$$ .getProfiler();
                //#else
                .mcProfiler;
                //#endif
        profiler.startSection("oneconfig_renderlivingentity_event_post");

        //#if MC >= 1.16.5
        //$$ double x = entity.getX();
        //$$ double y = entity.getY();
        //$$ double z = entity.getZ();
        //#endif
        RenderLivingEntityEvent event = new RenderLivingEntityEvent.Post(entity, partialTicks, x, y, z);
        EventManager.INSTANCE.post(event);
        // Can't cancel when the method has already returned lol

        profiler.endSection();
    }

}

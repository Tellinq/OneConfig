package org.polyfrost.oneconfig.internal.mixin.events;

//#if FORGE && MC <= 1.12.2
import net.minecraftforge.client.GuiIngameForge;
//#else
//$$ import net.minecraft.client.gui.GuiIngame;
//#endif

import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.HudRenderEvent;
import org.polyfrost.universal.UMatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if FORGE && MC <= 1.12.2
@Mixin(GuiIngameForge.class)
//#else
//$$ @Mixin(GuiIngame.class)
//#endif
public class Mixin_HudRenderEvent {

    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
    private void renderHudCallback(
            //#if MC >= 1.13
            //$$ MatrixStack matrixStack,
            //#endif
            float partialTicks,
            CallbackInfo ci
    ) {
        UMatrixStack stack =
                //#if MC >= 1.13
                //$$ new UMatrixStack(matrixStack);
                //#else
                new UMatrixStack();
                //#endif

        EventManager.INSTANCE.post(new HudRenderEvent(stack, partialTicks));
    }

}

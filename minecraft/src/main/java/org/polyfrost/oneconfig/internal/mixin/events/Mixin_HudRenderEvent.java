package org.polyfrost.oneconfig.internal.mixin.events;

//#if FORGE && MC <= 1.12.2
import net.minecraftforge.client.GuiIngameForge;
//#else
//#if FORGE-LIKE
//$$ import net.minecraft.client.gui.Gui;
//#else
//$$ import net.minecraft.client.gui.hud.InGameHud;
//#endif
//$$
//#if MC >= 1.20
//#if FORGE-LIKE
//$$ import net.minecraft.client.gui.GuiGraphics;
//#else
//$$ import net.minecraft.client.gui.DrawContext;
//#endif
//#elseif MC >= 1.13
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//#endif
//#endif

import dev.deftu.omnicore.client.render.OmniMatrixStack;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.HudRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if FORGE && MC <= 1.12.2
@Mixin(GuiIngameForge.class)
//#elseif FORGE-LIKE
//$$ @Mixin(Gui.class)
//#else
//$$ @Mixin(InGameHud.class)
//#endif
public class Mixin_HudRenderEvent {

    @Inject(
            //#if FORGE && MC <= 1.12.2
            method = "renderGameOverlay",
            //#else
            //$$ method = "render",
            //#endif
            at = @At("TAIL")
    )
    private void renderHudCallback(
            //#if MC >= 1.20
            //#if FORGE-LIKE
            //$$ GuiGraphics ctx,
            //#else
            //$$ DrawContext ctx,
            //#endif
            //#elseif MC >= 1.13
            //$$ PoseStack ctx,
            //#endif
            float partialTicks,
            CallbackInfo ci
    ) {
        OmniMatrixStack stack = OmniMatrixStack.vanilla(
                //#if MC >= 1.16.5
                //$$ ctx
                //#endif
        );

        EventManager.INSTANCE.post(new HudRenderEvent(stack, partialTicks));
    }

}

package org.polyfrost.oneconfig.internal.mixin.events;

//#if FORGE
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
//#else
//$$ import org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//#endif

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Minecraft.class)
public class Mixin_ScreenOpenEvent {

    //#if FORGE
    @ModifyArg(method = "displayGuiScreen", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", remap = false))
    private Event screenOpenCallback(Event a) {
        if (a instanceof GuiOpenEvent) {
            // w: not imported because 1.18+ they renamed it to be the same (breh)
            GuiOpenEvent forgeEvent = (GuiOpenEvent) a;
            org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent event =
                    new org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent(forgeEvent.
                            //#if MC<=10809
                            gui
                            //#else
                            //$$ getGui()
                            //#endif
                    );
            EventManager.INSTANCE.post(event);
            if (event.cancelled) {
                forgeEvent.setCanceled(true);
            }
            return forgeEvent;
        }
        return a;
    }
    //#else
    //$$  @Inject(method = "openScreen", at = @At(value = "INVOKE", target =
    //#if MC >= 1.13
    //$$  "Lnet/minecraft/client/network/ClientPlayerEntity;requestRespawn()V", shift = At.Shift.BY, by = 2
    //#elseif MC >= 1.12
    //$$  "Lnet/minecraft/client/gui/screen/DeathScreen;<init>(Lnet/minecraft/text/Text;)V", shift = At.Shift.BY, by = 3
    //#else
    //$$  "Lnet/minecraft/client/gui/screen/DeathScreen;<init>()V", shift = At.Shift.BY, by = 3
    //#endif
    //$$  ), cancellable = true)
    //$$  private void screenOpenCallback(net.minecraft.client.gui.screen.Screen screen, CallbackInfo ci) {
    //$$      ScreenOpenEvent event = new ScreenOpenEvent(screen);
    //$$      EventManager.INSTANCE.post(event);
    //$$      if (event.cancelled) {
    //$$          ci.cancel();
    //$$      }
    //$$  }
    //#endif

}

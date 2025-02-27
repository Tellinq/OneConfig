package org.polyfrost.oneconfig.internal.mixin.events;

//#if FORGE-LIKE
//#if MC >= 1.20.4
//$$ import net.minecraft.client.gui.screens.Screen;
//#else
//#if MC >= 1.19.2
//$$ import net.minecraftforge.client.event.ScreenEvent;
//#elseif MC >= 1.18.2
//$$ import net.minecraftforge.client.event.ScreenOpenEvent;
//#else
import net.minecraftforge.client.event.GuiOpenEvent;
//#endif
import net.minecraftforge.fml.common.eventhandler.Event;
//#endif
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

    //#if FORGE-LIKE
    @ModifyArg(
            method = "displayGuiScreen",
            at = @At(
                    value = "INVOKE",
                    //#if MC >= 1.20.4
                    //$$ target = "Lnet/minecraftforge/client/event/ForgeEventFactoryClient;onScreenOpening(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/screens/Screen;)Lnet/minecraft/client/gui/screens/Screen;",
                    //#elseif MC >= 1.16.5
                    //$$ target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z",
                    //#else
                    target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z",
                    //#endif
                    remap = false
            )
    )
    //#if MC >= 1.20.4
    //$$ private Screen screenOpenCallback(Screen oldScreen, Screen newScreen) {
    //$$     org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent event = new org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent(newScreen);
    //$$     EventManager.INSTANCE.post(event);
    //$$     if (event.cancelled) {
    //$$         return oldScreen;
    //$$     }
    //$$
    //$$     return event.getScreen();
    //$$ }
    //#else
    private Event screenOpenCallback(Event a) {
        //#if MC >= 1.19.2
        //$$ if (a instanceof ScreenEvent.Opening) {
        //$$     ScreenEvent.Opening forgeEvent = (ScreenEvent.Opening) a;
        //#elseif MC >= 1.18.2
        //$$ if (a instanceof ScreenOpenEvent) {
        //$$     ScreenOpenEvent forgeEvent = (ScreenOpenEvent) a;
        //#else
        if (a instanceof GuiOpenEvent) {
            GuiOpenEvent forgeEvent = (GuiOpenEvent) a;
        //#endif

            // w: not imported because 1.18+ they renamed it to be the same (breh)
            org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent event =
                    new org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent(forgeEvent.
                            //#if MC >= 1.18.2
                            //$$ getScreen()
                            //#elseif MC >= 1.12.2
                            //$$ getGui()
                            //#else
                            gui
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
    //#endif
    //#else
    //$$  @Inject(method = "setScreen", at = @At(value = "INVOKE", target =
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

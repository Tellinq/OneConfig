package org.polyfrost.oneconfig.internal.mixin.events;

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.WindowFocusEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_ModernWindowFocusEvent {
    @Inject(method = "setWindowActive", at = @At("HEAD"))
    private void onGameFocused(boolean focused, CallbackInfo ci) {
        if (focused) EventManager.INSTANCE.post(WindowFocusEvent.Gained.INSTANCE);
        else EventManager.INSTANCE.post(WindowFocusEvent.Lost.INSTANCE);
    }
}

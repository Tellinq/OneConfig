package org.polyfrost.oneconfig.internal.mixin.events;

import net.minecraft.client.network.NetHandlerLoginClient;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.ServerJoinEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerLoginClient.class)
public class Mixin_ServerJoinEvent {

    @Inject(method = "handleLoginSuccess", at = @At("RETURN"))
    private void onLoginSuccess(CallbackInfo ci) {
        EventManager.INSTANCE.post(ServerJoinEvent.INSTANCE);
    }

}

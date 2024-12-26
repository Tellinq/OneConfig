package org.polyfrost.oneconfig.internal.mixin.events;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.SendPacketEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class Mixin_ReceivePacketEvent {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void packetSendCallback(Packet<?> packetIn, CallbackInfo ci) {
        SendPacketEvent event = new SendPacketEvent(packetIn);
        EventManager.INSTANCE.post(event);
        if (event.cancelled) {
            ci.cancel();
        }
    }

}

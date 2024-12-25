package org.polyfrost.oneconfig.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovingObjectPosition;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_PlayerInteractEvent_LeftAction {

    @Shadow public MovingObjectPosition objectMouseOver;
    @Shadow public EntityPlayerSP thePlayer;

    @Shadow private int leftClickCounter;
    @Unique private PlayerInteractEvent lastAttackEvent;

    @Inject(
            method = "clickMouse",
            at = @At(
                    //#if MC >= 1.12.2
                    //$$ value = "HEAD"
                    //#else
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;swingItem()V",
                    shift = At.Shift.BEFORE
                    //#endif
            ),
            cancellable = true
    )
    private void onPlayerAttackCallback(CallbackInfo ci) {
        if (this.leftClickCounter > 0) {
            return;
        }

        MovingObjectPosition rayCastedObject = this.objectMouseOver;
        PlayerInteractEvent.Type type = PlayerInteractEvent.Type.AIR;
        if (rayCastedObject != null) {
            switch (rayCastedObject.typeOfHit) {
                case BLOCK:
                    type = PlayerInteractEvent.Type.BLOCK;
                    break;
                case ENTITY:
                    type = PlayerInteractEvent.Type.ENTITY;
                    break;
            }
        }

        lastAttackEvent = new PlayerInteractEvent(this.thePlayer, PlayerInteractEvent.Action.LEFT, type);
        EventManager.INSTANCE.post(lastAttackEvent);
        if (lastAttackEvent.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    private void onBlockClickPacketCallback(boolean leftClick, CallbackInfo ci) {
        if (lastAttackEvent != null && lastAttackEvent.getAction() == PlayerInteractEvent.Action.LEFT && lastAttackEvent.cancelled) {
            ci.cancel();
        }

        if (!leftClick) {
            lastAttackEvent = null;
        }
    }

}

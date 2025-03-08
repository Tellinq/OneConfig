package org.polyfrost.oneconfig.internal.mixin.events;

//#if MC >= 1.18.2
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

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
public abstract class Mixin_PlayerInteractEvent_LeftAction {

    @Shadow public MovingObjectPosition objectMouseOver;
    @Shadow public EntityPlayerSP thePlayer;

    @Shadow private int leftClickCounter;
    @Unique private PlayerInteractEvent ocfg$lastAttackEvent;

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
    private void onPlayerAttackCallback(
            //#if MC >= 1.18.2
            //$$ CallbackInfoReturnable<Boolean> cir
            //#else
            CallbackInfo ci
            //#endif
    ) {
        if (this.leftClickCounter > 0) {
            return;
        }

        MovingObjectPosition rayCastedObject = this.objectMouseOver;
        PlayerInteractEvent.Type type = PlayerInteractEvent.Type.AIR;
        if (rayCastedObject != null) {
            MovingObjectPosition.MovingObjectType typeOfHit =
                    //#if MC >= 1.16.5
                    //$$ rayCastedObject.getType();
                    //#else
                    rayCastedObject.typeOfHit;
                    //#endif
            switch (typeOfHit) {
                case BLOCK:
                    type = PlayerInteractEvent.Type.BLOCK;
                    break;
                case ENTITY:
                    type = PlayerInteractEvent.Type.ENTITY;
                    break;
            }
        }

        ocfg$lastAttackEvent = new PlayerInteractEvent(this.thePlayer, PlayerInteractEvent.Action.LEFT, type);
        EventManager.INSTANCE.post(ocfg$lastAttackEvent);
        if (ocfg$lastAttackEvent.cancelled) {
            //#if MC >= 1.18.2
            //$$ cir.setReturnValue(false);
            //#else
            ci.cancel();
            //#endif
        }
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    private void onBlockClickPacketCallback(boolean leftClick, CallbackInfo ci) {
        if (ocfg$lastAttackEvent != null && ocfg$lastAttackEvent.getAction() == PlayerInteractEvent.Action.LEFT && ocfg$lastAttackEvent.cancelled) {
            ci.cancel();
        }

        if (!leftClick) {
            ocfg$lastAttackEvent = null;
        }
    }

}

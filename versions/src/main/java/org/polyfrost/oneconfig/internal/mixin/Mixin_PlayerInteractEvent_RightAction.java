package org.polyfrost.oneconfig.internal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MovingObjectPosition;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_PlayerInteractEvent_RightAction {

    @Shadow public MovingObjectPosition objectMouseOver;
    @Shadow public EntityPlayerSP thePlayer;

    @Inject(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onPlayerInteractCallback(CallbackInfo ci) {
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

        PlayerInteractEvent event = new PlayerInteractEvent(this.thePlayer, PlayerInteractEvent.Action.RIGHT, type);
        EventManager.INSTANCE.post(event);
        if (event.cancelled) {
            ci.cancel();
        }
    }

}

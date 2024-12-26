package org.polyfrost.oneconfig.internal.mixin.events;

//#if MC >= 1.16.5
//$$ import org.spongepowered.asm.mixin.injection.ModifyVariable;
//$$ import net.minecraft.client.KeyboardListener;
//#else
import net.minecraft.client.Minecraft;
//#endif

import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.KeyInputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 1.16.5
//$$ @Mixin(KeyboardListener.class)
//#else
@Mixin(Minecraft.class)
//#endif
public class Mixin_KeyInputEvent {

    //#if MC >= 1.16.5
    //$$ @ModifyVariable(method = "onKeyEvent", at = @At(value = "STORE"), ordinal = 0)
    //$$ private boolean keyCallback(boolean original, long windowPointer, int key, int scanCode, int action, int modifiers) {
    //$$     EventManager.INSTANCE.post(new KeyInputEvent(key, (char) 0, action));
    //$$     return original;
    //$$ }
    //$$
    //$$ @ModifyVariable(method = "onCharEvent", at = @At(value = "STORE"), ordinal = 0)
    //$$ private boolean charCallback(boolean original, long windowPointer, char key, int code, int modifiers) {
    //$$     EventManager.INSTANCE.post(new KeyInputEvent(0, key, 1));
    //$$     return original;
    //$$ }
    //#else

    //@formatter:off
    //#if MC <= 1.8.9
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V", ordinal = 1))
    //#else
    //#if FORGE
    //$$ @Inject(method = "runTickKeyboard", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugCrashKeyPressTime:J", opcode = org.objectweb.asm.Opcodes.PUTFIELD))
    //#else
    //$$ @Inject(method = "method_12145", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;f3CTime:J", opcode = org.objectweb.asm.Opcodes.PUTFIELD))
    //#endif
    //#endif
    //@formatter:on
    private void keyCallback(CallbackInfo ci) {
        int state = 0;
        if (org.lwjgl.input.Keyboard.getEventKeyState()) {
            if (org.lwjgl.input.Keyboard.isRepeatEvent()) {
                state = 2;
            } else {
                state = 1;
            }
        }
        EventManager.INSTANCE.post(new KeyInputEvent(org.lwjgl.input.Keyboard.getEventKey(), org.lwjgl.input.Keyboard.getEventCharacter(), state));
    }

    //#endif

}

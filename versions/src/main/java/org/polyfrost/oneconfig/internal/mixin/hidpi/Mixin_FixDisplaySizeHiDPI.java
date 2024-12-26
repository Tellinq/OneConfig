package org.polyfrost.oneconfig.internal.mixin.hidpi;

//#if MC < 1.13
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class Mixin_FixDisplaySizeHiDPI {

    //@formatter:off
    @Shadow public int displayWidth;
    @Shadow public int displayHeight;
    @Shadow private int tempDisplayWidth;
    @Shadow private int tempDisplayHeight;
    //@formatter:on

    @ModifyVariable(method = "resize", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private int hiDpiFixResizeW(int value) {
        return (int) (value * Display.getPixelScaleFactor());
    }

    @ModifyVariable(method = "resize", at = @At(value = "HEAD"), ordinal = 1, argsOnly = true)
    private int hiDpiFixResizeH(int value) {
        return (int) (value * Display.getPixelScaleFactor());
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createDisplay()V", shift = At.Shift.AFTER))
    private void hiDpiFixDisplaySizes(CallbackInfo ci) {
        float scale = Display.getPixelScaleFactor();
        this.displayWidth = (int) (this.displayWidth * scale);
        this.displayHeight = (int) (this.displayHeight * scale);
        this.tempDisplayWidth = (int) (this.tempDisplayWidth * scale);
        this.tempDisplayHeight = (int) (this.tempDisplayHeight * scale);
    }

}
//#endif

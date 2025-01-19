package org.polyfrost.oneconfig.internal.mixin;

//#if MC >= 1.16.5
//$$ import net.minecraft.client.Minecraft;
//$$ import org.polyfrost.oneconfig.internal.OneConfig;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ @Mixin(Minecraft.class)
//$$ public class Mixin_ModernEntrypoint {
//$$
//$$     @Unique
//$$     private boolean ocfg$initialized = false;
//$$
//$$     @Inject(method = "<init>", at = @At("RETURN"))
//$$     private void ocfg$entrypoint(CallbackInfo ci) {
//$$         if (ocfg$initialized) {
//$$             return;
//$$         }
//$$
//$$         OneConfig.INSTANCE.init();
//$$         ocfg$initialized = true;
//$$     }
//$$
//$$ }
//#endif

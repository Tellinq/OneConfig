package org.polyfrost.oneconfig.internal.mixin;

//#if MC <= 1.8.9
import net.minecraft.client.audio.SoundEventAccessorComposite;
//#endif

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.SoundPlayedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @ModifyVariable(
            method = "playSound",
            at = @At(
                    value = "INVOKE",
                    //#if MC >= 1.12.2
                    //#if MC == 1.12.2 && FABRIC
                    //$$ target = "Lnet/minecraft/client/sound/SoundInstance;method_12532(Lnet/minecraft/client/sound/SoundManager;)Lnet/minecraft/client/sound/SoundContainerImpl;",
                    //#else
                    //$$ target = "Lnet/minecraft/client/audio/ISound;createAccessor(Lnet/minecraft/client/audio/SoundHandler;)Lnet/minecraft/client/audio/SoundEventAccessor;",
                    //#endif
                    //#else
                    target = "Lnet/minecraft/client/audio/SoundHandler;getSound(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/audio/SoundEventAccessorComposite;",
                    //#endif
                    shift = At.Shift.BEFORE
            ),
            argsOnly = true
    )
    private ISound oneconfig$playSound(ISound value) {
        //#if MC <= 1.8.9
        SoundEventAccessorComposite accessor = ((SoundManagerAccessorMixin) this).getSndHandler().getSound(value.getSoundLocation());
        SoundCategory category = (accessor == null ? null : accessor.getSoundCategory());
        //#else
        //#if MC == 1.12.2 && FABRIC
        //$$ SoundCategory category = value.method_12534();
        //#else
        //$$ SoundCategory category = value.getCategory();
        //#endif
        //#endif

        String name = value.getSoundLocation().getResourcePath();
        SoundPlayedEvent event = new SoundPlayedEvent(name, category, value);
        EventManager.INSTANCE.post(event);

        return event.getSound();
    }

}

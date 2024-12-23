package org.polyfrost.oneconfig.internal.mixin;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessorComposite;
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
                    target = "Lnet/minecraft/client/audio/SoundHandler;getSound(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/audio/SoundEventAccessorComposite;"
            ),
            argsOnly = true
    )
    private ISound oneconfig$playSound(ISound value) {
        SoundEventAccessorComposite accessor = ((SoundManager) (Object) this).sndHandler.getSound(value.getSoundLocation());
        String name = value.getSoundLocation().getResourcePath();
        SoundPlayedEvent event = new SoundPlayedEvent(name, (accessor == null ? null : accessor.getSoundCategory()), value);
        EventManager.INSTANCE.post(event);
        return event.getSound();
    }

}

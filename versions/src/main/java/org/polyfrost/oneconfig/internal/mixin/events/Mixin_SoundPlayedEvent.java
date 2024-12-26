package org.polyfrost.oneconfig.internal.mixin.events;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundManager;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.SoundPlayedEvent;
import org.polyfrost.oneconfig.internal.mixin.Mixin_SoundHandlerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundManager.class)
public class Mixin_SoundPlayedEvent {

    @Shadow
    private boolean loaded;

    @ModifyVariable(
            method = "playSound",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ISound onPlaySoundCallback(ISound value) {
        if (!this.loaded) {
            return value;
        }

        //#if MC <= 1.8.9
        SoundEventAccessorComposite accessor = ((Mixin_SoundHandlerAccessor) this).getSndHandler().getSound(value.getSoundLocation());
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

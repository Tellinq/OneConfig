package org.polyfrost.oneconfig.api.event.v1.events;

import org.polyfrost.universal.UMatrixStack;

public class PostWorldRenderEvent implements Event {

    private final UMatrixStack stack;

    private final float partialTicks;

    public PostWorldRenderEvent(UMatrixStack stack, float partialTicks) {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }

    public UMatrixStack getStack() {
        return stack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

}

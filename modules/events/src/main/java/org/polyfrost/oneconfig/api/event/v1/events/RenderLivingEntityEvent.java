package org.polyfrost.oneconfig.api.event.v1.events;

public abstract class RenderLivingEntityEvent extends Event.Cancellable {

    private final Object entity;
    private final float partialTicks;
    private final double x, y, z;

    public RenderLivingEntityEvent(Object entity, float partialTicks, double x, double y, double z) {
        this.entity = entity;
        this.partialTicks = partialTicks;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Object getEntity() {
        return entity;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public static class Pre extends RenderLivingEntityEvent {
        public Pre(Object entity, float partialTicks, double x, double y, double z) {
            super(entity, partialTicks, x, y, z);
        }
    }

    public static class Post extends RenderLivingEntityEvent {
        public Post(Object entity, float partialTicks, double x, double y, double z) {
            super(entity, partialTicks, x, y, z);
        }
    }

}

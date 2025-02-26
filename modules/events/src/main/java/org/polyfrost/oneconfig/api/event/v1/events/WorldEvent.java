package org.polyfrost.oneconfig.api.event.v1.events;

public abstract class WorldEvent implements Event {
    private final Object world;

    private WorldEvent(Object world) {
        this.world = world;
    }

    public static final class Load extends WorldEvent {
        public Load(Object world) {
            super(world);
        }
    }

    public static final class Unload extends WorldEvent {
        public Unload(Object world) {
            super(world);
        }
    }

    /**
     * Due to differences across Minecraft versions, this is a Duck method, meaning that it will return the expected type for that minecraft version.
     * <ul>
     *     <li>For modern forge, this will be a ClientLevel.</li>
     *     <li>For fabric & forge pre-1.17, this will be a ClientWorld.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public <T> T getWorld() {
        return (T) world;
    }

    public Object component1() {
        return world;
    }
}

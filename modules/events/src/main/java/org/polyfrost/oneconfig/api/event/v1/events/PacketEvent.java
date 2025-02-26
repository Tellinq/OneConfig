package org.polyfrost.oneconfig.api.event.v1.events;

public abstract class PacketEvent extends Event.Cancellable {
    private final Object packet;

    public PacketEvent(Object packet) {
        this.packet = packet;
    }

    public static final class Send extends PacketEvent {
        public Send(Object packet) {
            super(packet);
        }
    }

    public static final class Receive extends PacketEvent {
        public Receive(Object packet) {
            super(packet);
        }
    }

    /**
     * Due to differences across Minecraft versions, this is a Duck method, meaning that it will return the expected type for that minecraft version.
     * <ul>
     *     <li>For legacy forge, this will be a IPacket.</li>
     *     <li>For modern forge, this will be a Packet.</li>
     *     <li>For fabric, this will be a Packet.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public <T> T getPacket() {
        return (T) packet;
    }

    public Object component1() {
        return packet;
    }
}

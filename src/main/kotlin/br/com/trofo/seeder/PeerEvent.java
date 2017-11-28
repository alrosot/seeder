package br.com.trofo.seeder;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public enum PeerEvent {

    none(0),
    completed(1),
    started(2),
    stopped(3);

    private int udpCode;

    PeerEvent(int udpCode) {
        this.udpCode = udpCode;
    }

    public static PeerEvent getByUdpCode(String udpCode) {
        int parsedCode = Integer.parseInt(udpCode);
        return Arrays.stream(values()).filter(peerEvent -> peerEvent.udpCode == parsedCode).findFirst().orElseThrow(() -> new IllegalArgumentException("Event not found for " + udpCode));
    }

    public static Optional<PeerEvent> valueIfPresent(@Nullable String eventType) {
        if (eventType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(PeerEvent.valueOf(eventType));
    }
}

package io.github.term4.minestommechanics.platform.client;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.nio.charset.StandardCharsets;

public final class VersionDetector {

    // This class should detect the protocol version (MAYBE client version if the client is using viaFabric, but this is spoofable) of the player on joining,
    //  and store it in the ClientInfoService map. This listener should only be active if configured (i.e. cross version play enabled)

    // For now only use protocol version sent from the proxy

    // COULD switch this to use tags, but would probably get messy. Map is better.

    public static final String VIA_PROXY_DETAILS_CHANNEL = "vv:proxy_details";

    private VersionDetector() {}

    public static EventNode<PlayerEvent> node(ClientInfoService clientInfo) {
        EventNode<PlayerEvent> node = EventNode.type("mm:via-proxy-details", EventFilter.PLAYER);

        node.addListener(PlayerPluginMessageEvent.class, e -> {
            if (!VIA_PROXY_DETAILS_CHANNEL.equals(e.getIdentifier())) return;

            byte[] data = e.getMessage();
            if (data.length == 0) return;

            // Via payload is UTF-8 JSON
            String json = new String(data, StandardCharsets.UTF_8);
            clientInfo.setProxyDetails(e.getPlayer(), json);
        });

        return node;
    }
}

package io.github.term4.minestommechanics.platform.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Can probably replace this entire thing with custom player and just track / attach data there?
public final class ClientInfoService {

    // This class stores ONLY client information relevant to this library. For now only the protocol version.
    // In the future this may include things like modifiers, Animatium, Combatify, Lunar client Apollo, and Badlion's API

    public static final int UNKNOWN_PROTOCOL = -1;

    // Client information object: contains all relevant information about the client
    public static final class ClientInfo {
        String proxyDetails;
        Integer cachedProtocol;
    }

    // Map linking a player uuid to that players ClientInfo object
    private final ConcurrentHashMap<UUID, ClientInfo> clients = new ConcurrentHashMap<>();

    /** Gets or creates a new entry for a client */
    private ClientInfo getOrCreate(Player player) {
        return clients.computeIfAbsent(player.getUuid(), id -> new ClientInfo());
    }

    /** Store ViaVersion proxy details JSON */
    public void setProxyDetails(Player player, String proxyDetails) {
        ClientInfo info = getOrCreate(player);
        info.proxyDetails = proxyDetails;
        info.cachedProtocol = null;
    }

    public String getProxyDetails(Player player) {
        ClientInfo info = clients.get(player.getUuid());
        return info == null ? null : info.proxyDetails;
    }

    /**
     * Returns the player's protocol version from the ViaVersion proxy details.
     * <p>
     * NOTE:
     * This value may return {@link #UNKNOWN_PROTOCOL} (-1) until the plugin
     * message is received from the proxy. That message is typically sent
     * shortly after login (not during the initial join events).
     * <p>
     * Systems relying on the protocol version should handle the UNKNOWN case
     * or defer execution briefly after login.
     */
    public int getProtocol(Player player) {
        ClientInfo info = clients.get(player.getUuid());
        if (info == null) return UNKNOWN_PROTOCOL;

        if (info.cachedProtocol != null) return info.cachedProtocol;

        String json = info.proxyDetails;
        if (json == null || json.isEmpty()) return UNKNOWN_PROTOCOL;

        int parsed = parseProtocol(json);
        info.cachedProtocol = (parsed == UNKNOWN_PROTOCOL) ? null : parsed; // cache only valid
        return parsed;
    }

    private static int parseProtocol(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            var version = obj.get("version");
            return (version != null && version.isJsonPrimitive()) ? version.getAsInt() : UNKNOWN_PROTOCOL;
        } catch (Exception ignored) {
            return UNKNOWN_PROTOCOL;
        }
    }

    public void remove(Player player) {
        clients.remove(player.getUuid());
    }
}

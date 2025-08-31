package net.hellomouse.network;

import net.hellomouse.HeadpatMod;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class HeadpatNetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final Identifier PAT_SYNC_IDENTIFIER = Identifier.fromNamespaceAndPath(HeadpatMod.MOD_ID, "pat_sync");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        PAT_SYNC_IDENTIFIER,
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++,
            PatSyncPacket.class,
            PatSyncPacket::encode,
            PatSyncPacket::decode,
            PatSyncPacket::handle);
    }
}
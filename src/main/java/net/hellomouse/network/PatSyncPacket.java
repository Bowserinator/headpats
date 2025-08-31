package net.hellomouse.network;

import net.hellomouse.HeadpatMod;
import net.hellomouse.patstate.PatState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class PatSyncPacket {
    private final UUID patter, pattee;
    private final boolean patting;

    public PatSyncPacket(UUID patter, UUID pattee, boolean patting) {
        this.patter = patter;
        this.pattee = pattee;
        this.patting = patting;
    }

    public static void encode(PatSyncPacket msg, PacketByteBuf buf) {
        buf.writeUuid(msg.patter);
        buf.writeUuid(msg.pattee);
        buf.writeBoolean(msg.patting);
    }

    public static PatSyncPacket decode(PacketByteBuf buf) {
        return new PatSyncPacket(buf.readUuid(), buf.readUuid(), buf.readBoolean());
    }

    // For packets received from server
    public static void handleToClientPacket(PatSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        if (msg.patting) PatState.CLIENT_INSTANCE.startPatting(msg.patter, msg.pattee);
        else             PatState.CLIENT_INSTANCE.endPatting(msg.patter, msg.pattee);
    }

    // For packets sent to server
    public static void handleFromClientPacket(PatSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // Validate sender + broadcast pat state update
        ServerPlayerEntity player = ctx.get().getSender();
        if (player != null && msg.patter.equals(player.getUuid())) {
            ServerWorld world = player.getServerWorld();
            if (world == null) return;

            PlayerEntity target = world.getPlayerByUuid(msg.pattee);
            if (target == null) return;

            if (msg.patting) PatState.SERVER_INSTANCE.startPatting(msg.patter, msg.pattee);
            else             PatState.SERVER_INSTANCE.endPatting(msg.patter, msg.pattee);
            HeadpatNetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(),
                    new PatSyncPacket(msg.patter, msg.pattee, msg.patting));
        }
    }

    public static void handle(PatSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PatSyncPacket.handleToClientPacket(msg, ctx)));
        ctx.get().enqueueWork(() -> PatSyncPacket.handleFromClientPacket(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}

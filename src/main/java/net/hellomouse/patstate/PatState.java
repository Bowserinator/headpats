package net.hellomouse.patstate;

import net.hellomouse.PatUtil;
import net.hellomouse.network.HeadpatNetworkHandler;
import net.hellomouse.network.PatSyncPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PatState {
    public static final PatState SERVER_INSTANCE = new PatState(false);
    public static final PatState CLIENT_INSTANCE = new PatState(true);

    // Track patter -> pattee relations
    private final HashMap<UUID, UUID> patterToPattee = new HashMap<>();
    private final HashMap<UUID, PlayerPatState> playerToState = new HashMap<>();

    public final boolean isClient;
    private long clientLastInteract = 0;

    public PatState(boolean isClient) {
        this.isClient = isClient;
    }

    // -------------- Client ---------------------

    @OnlyIn(Dist.CLIENT)
    public void startPattingClient(PlayerEntity player, PlayerEntity target) {
        if (!patterToPattee.containsKey(player.getUuid())) {
            HeadpatNetworkHandler.CHANNEL.sendToServer(new PatSyncPacket(player.getUuid(), target.getUuid(), true));
            startPatting(player, target);
        }
        clientLastInteract = player.getWorld().getTime();
    }

    @OnlyIn(Dist.CLIENT)
    public void endPattingClient(PlayerEntity player, PlayerEntity target) {
        endPattingClient(player.getUuid(), target.getUuid());
    }

    @OnlyIn(Dist.CLIENT)
    public void endPattingClient(UUID player, UUID target) {
        endPatting(player, target);
        HeadpatNetworkHandler.CHANNEL.sendToServer(new PatSyncPacket(player, target, false));
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (MinecraftClient.getInstance().world == null)
            return;

        var world = MinecraftClient.getInstance().world;
        patterToPattee.entrySet().removeIf(uuidIntegerEntry ->
                world.getPlayerByUuid(uuidIntegerEntry.getKey()) == null ||
                world.getPlayerByUuid(uuidIntegerEntry.getValue()) == null);
        playerToState.entrySet().removeIf(uuidPlayerPatStateEntry -> world.getPlayerByUuid(uuidPlayerPatStateEntry.getKey()) == null);

        int AUTO_REMOVE_TICKS = 4;
        if (world.getTime() - clientLastInteract > AUTO_REMOVE_TICKS) {
            PlayerEntity thisPlayer = MinecraftClient.getInstance().player;
            if (thisPlayer != null && patterToPattee.containsKey(thisPlayer.getUuid()))
                endPattingClient(thisPlayer.getUuid(), patterToPattee.get(thisPlayer.getUuid()));
        }

        for (var entry : playerToState.entrySet()) {
            entry.getValue().tick();
        }
    }

    // ----------------- Server  -------------------

    public void serverTick(int ticks, MinecraftServer server) {
        patterToPattee.entrySet().removeIf(uuidIntegerEntry ->
                server.getPlayerManager().getPlayer(uuidIntegerEntry.getKey()) == null ||
                server.getPlayerManager().getPlayer(uuidIntegerEntry.getValue()) == null);

        // Invalidate illegal pats
        HashSet<UUID> illegalPats = new HashSet<>();
        for (var entry : patterToPattee.entrySet()) {
            PlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            PlayerEntity target = server.getPlayerManager().getPlayer(entry.getValue());
            if (target == null || player == null)
                continue;
            if (target.getWorld() != player.getWorld() ||
                    target.getWorld().getDimension() != player.getWorld().getDimension() ||
                    !PatUtil.canHeadpatServer(player, target))
                illegalPats.add(entry.getKey());
        }
        for (var uuid : illegalPats) {
            HeadpatNetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(),
                    new PatSyncPacket(uuid, patterToPattee.get(uuid), false));
            endPatting(uuid, patterToPattee.get(uuid));
        }
    }

    public void serverSendToClient(MinecraftServer server, ServerPlayerEntity serverPlayerEntity) {
        for (var entry : patterToPattee.entrySet()) {
            PlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            PlayerEntity target = server.getPlayerManager().getPlayer(entry.getValue());
            if (target == null || player == null)
                continue;
            HeadpatNetworkHandler.CHANNEL.sendTo(
                    new PatSyncPacket(player.getUuid(), target.getUuid(), true),
                    serverPlayerEntity.networkHandler.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }
    }

    // ----------------- Shared -------------------

    public void startPatting(PlayerEntity player, PlayerEntity target) { startPatting(player.getUuid(), target.getUuid()); }
    public void endPatting(PlayerEntity player, PlayerEntity target) {   endPatting(player.getUuid(), target.getUuid()); }
    public void startPatting(UUID player, UUID target) {
        patterToPattee.put(player, target);
        if (isClient) {
            addPlayerState(player);
            addPlayerState(target);
        }
    }
    public void endPatting(UUID player, UUID target) {
        patterToPattee.remove(player, target);
    }

    public @Nullable PlayerPatState get(PlayerEntity player) { return get(player.getUuid()); }
    public @Nullable PlayerPatState get(UUID player) { return playerToState.get(player); }

    @OnlyIn(Dist.CLIENT)
    public void addPlayerState(UUID player) {
        var world = MinecraftClient.getInstance().world;
        assert world != null;
        PlayerEntity playerE = world.getPlayerByUuid(player);
        if (playerE != null)
            playerToState.put(player, new PlayerPatState(playerE, this));
    }

    public boolean isUserPatting(UUID player) {
        return patterToPattee.containsKey(player);
    }
    public boolean isUserBeingPatted(UUID player) {
        for (var entry : patterToPattee.entrySet()) {
            if (entry.getValue().equals(player))
                return true;
        }
        return false;
    }
    public boolean isUserPatting(PlayerEntity player) {
        return isUserPatting(player.getUuid());
    }
    public boolean isUserBeingPatted(PlayerEntity player) {
        return isUserBeingPatted(player.getUuid());
    }
}

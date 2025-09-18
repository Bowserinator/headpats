package net.hellomouse;

import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.hellomouse.network.HeadpatNetworkHandler;
import net.hellomouse.patstate.PatState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HeadpatMod.MOD_ID)
public final class HeadpatMod {
    public static final String MOD_ID = "headpats";
    public static final Logger LOG = LogUtils.getLogger();
    public static HeadpatConfig CONFIG;

    public HeadpatMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        HeadpatNetworkHandler.register();

        LOG.info("Loading Headpats configuration...");
        AutoConfig.register(HeadpatConfig.class, Toml4jConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(HeadpatConfig.class).getConfig();
        modEventBus.register(this);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents {
        // Right click = headpat!
        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
            PlayerEntity player = event.getEntity();
            Entity target = event.getTarget();
            var hitPos = event.getLocalPos();

            if (target instanceof PlayerEntity targetPlayer) {
                if (PatUtil.canHeadpatInteract(hitPos, player, targetPlayer))
                    PatState.CLIENT_INSTANCE.startPattingClient(player, targetPlayer);
            }
        }

        // net sync + state update
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            PatState.CLIENT_INSTANCE.clientTick();
        }

        // net sync + state update
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                MinecraftServer server = event.getServer();
                PatState.SERVER_INSTANCE.serverTick(server.getTicks(), server);
            }
        }

        // First person roll when patted
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
            float roll = event.getRoll();
            if (event.getCamera().getFocusedEntity() instanceof PlayerEntity player) {
                float patRoll = PatRendering.getCameraRoll(player, (float)event.getPartialTick());
                event.setRoll(roll + (float)Math.toDegrees(patRoll));
            }
        }

        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            var player = event.getEntity();
            if (player instanceof ServerPlayerEntity serverPlayer)
                PatState.SERVER_INSTANCE.serverSendToClient(event.getEntity().getServer(), serverPlayer);
        }
    }
}

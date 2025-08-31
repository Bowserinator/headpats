package net.hellomouse.patstate;

import net.hellomouse.HeadpatConfig;
import net.hellomouse.HeadpatMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerPatState {
    // Client animation fields
    public int prevPettingTicks;
    public int pettingTicks;
    public float prevPettingMultiplier;
    public float pettingMultiplier;
    public int prevPettedTicks;
    public int pettedTicks;
    public float prevPettedMultiplier;
    public float pettedMultiplier;

    // Accessors
    private final PatState state;
    private final PlayerEntity player;

    PlayerPatState(PlayerEntity player, PatState state) {
        this.state = state;
        this.player = player;
        assert player != null;
    }

    void tick() {
        // Animation update
        prevPettingTicks = pettingTicks;
        prevPettingMultiplier = pettingMultiplier;

        if (state.isUserPatting(player)) {
            pettingTicks++;
            pettingMultiplier += (1 - pettingMultiplier) * 0.3f;
        } else {
            pettingMultiplier -= pettingMultiplier * 0.3f;
            if (pettingMultiplier < 0.01f) {
                pettingMultiplier = 0;
                pettingTicks = 0;
            }
        }

        prevPettedTicks = pettedTicks;
        prevPettedMultiplier = pettedMultiplier;

        if (state.isUserBeingPatted(player)) {
            if (pettedTicks % 40 == 0 && HeadpatMod.CONFIG.pettedPlayersPurr)
                MinecraftClient.getInstance().getSoundManager().play(
                    new PositionedSoundInstance(SoundEvents.ENTITY_CAT_PURR, SoundCategory.PLAYERS, 1F,
                            player.getSoundPitch(), player.getRandom(), player.getBlockPos())
                );

            pettedTicks++;
            pettedMultiplier += (1 - pettedMultiplier) * 0.3f;
        } else {
            pettedMultiplier -= pettedMultiplier * 0.3f;
            if (pettedMultiplier < 0.01f) {
                pettedMultiplier = 0;
                pettedTicks = 0;
            }
        }
    }
}

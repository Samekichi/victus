package com.glisco.victus.mixin;

import com.glisco.victus.Victus;
import com.glisco.victus.hearts.HeartAspectComponent;
import com.glisco.victus.hearts.content.LapisAspect;
import com.glisco.victus.network.VictusPackets;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

    @Shadow
    protected abstract int repairPlayerGears(PlayerEntity player, int amount);

    @Inject(method = "repairPlayerGears", at = @At("HEAD"), cancellable = true)
    private void healIfAspectPresent(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (amount < 4) return;

        final var aspects = Victus.ASPECTS.get(player);
        if (!aspects.hasActiveAspect(LapisAspect.TYPE)) return;

        final int lapisIndex = aspects.findFirstIndex(LapisAspect.TYPE);
        aspects.getAspect(lapisIndex).onBroken(DamageSource.OUT_OF_WORLD, 0, player.getHealth());
        VictusPackets.sendAspectBreak(serverPlayer, lapisIndex, false);

        player.heal(1);

        if (amount <= 4) cir.setReturnValue(0);
        cir.setReturnValue(repairPlayerGears(player, amount - 4));
    }

}
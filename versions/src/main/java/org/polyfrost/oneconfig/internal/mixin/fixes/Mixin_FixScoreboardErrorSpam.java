package org.polyfrost.oneconfig.internal.mixin.fixes;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class Mixin_FixScoreboardErrorSpam {

    @Inject(method = "removeTeam", at = @At("HEAD"), cancellable = true)
    private void checkNPETeam(ScorePlayerTeam team, CallbackInfo ci) {
        if (team == null) {
            ci.cancel();
        }
    }

    @Inject(method = "removeObjective", at = @At("HEAD"), cancellable = true)
    private void checkNPEObj(ScoreObjective objective, CallbackInfo ci) {
        if (objective == null) {
            ci.cancel();
        }
    }

}

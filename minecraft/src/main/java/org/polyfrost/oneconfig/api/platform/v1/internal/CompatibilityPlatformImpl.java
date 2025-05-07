package org.polyfrost.oneconfig.api.platform.v1.internal;

import dev.deftu.omnicore.common.OmniLoader;
import org.polyfrost.oneconfig.api.platform.v1.CompatibilityPlatform;

import java.util.HashSet;
import java.util.Set;

//#if FORGE && MC <= 1.12.2
import dev.deftu.omnicore.client.OmniClientPlayer;
import net.minecraftforge.client.ClientCommandHandler;
//#endif

public class CompatibilityPlatformImpl implements CompatibilityPlatform {

    public Set<OmniLoader.ModInfo> getValidTrees() {
        //#if MC >= 1.16.5 || FABRIC
        //$$ // Unneeded in 1.16.5+ / Legacy Fabric
        //$$ return new HashSet<>();
        //#else
        // Returns a set of mod trees which can be displayed as items in the config menu
        Set<OmniLoader.ModInfo> result = new HashSet<>();
        Set<OmniLoader.ModInfo> mods = OmniLoader.getLoadedMods();
        Set<String> commands = ClientCommandHandler.instance.getCommands().keySet();

        for (OmniLoader.ModInfo mod : mods) {
            if (!commands.contains(mod.getId())) {
                continue;
            }

            result.add(mod);
        }

        return result;
        //#endif
    }

    public void executeTreeAction(String action) {
        //#if MC >= 1.16.5 || FABRIC
        //$$ // Unneeded in 1.16.5+ / Legacy Fabric
        //#else
        // Executes the command for the given mod
        ClientCommandHandler.instance.executeCommand(OmniClientPlayer.getInstance(), action);
        //#endif
    }

}

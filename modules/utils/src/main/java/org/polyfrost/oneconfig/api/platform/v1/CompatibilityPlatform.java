package org.polyfrost.oneconfig.api.platform.v1;

import dev.deftu.omnicore.common.OmniLoader;

import java.util.Set;

public interface CompatibilityPlatform {

    Set<OmniLoader.ModInfo> getValidTrees();

    void executeTreeAction(String action);

}


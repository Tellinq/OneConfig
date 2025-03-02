/*
 * This file is part of OneConfig.
 * OneConfig - Next Generation Config Library for Minecraft: Java Edition
 * Copyright (C) 2021~2024 Polyfrost.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *   OneConfig is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation, AND
 * under the Additional Terms Applicable to OneConfig, as published by Polyfrost,
 * either version 1.0 of the Additional Terms, or (at your option) any later
 * version.
 *
 *   This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>. You should
 * have also received a copy of the Additional Terms Applicable
 * to OneConfig, as published by Polyfrost. If not, see
 * <https://polyfrost.org/legal/oneconfig/additional-terms>
 */

package org.polyfrost.oneconfig.internal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import dev.deftu.clipboard.Clipboard;
import dev.deftu.omnicore.client.OmniChat;
import dev.deftu.omnicore.client.OmniClientCommands;
import dev.deftu.omnicore.common.OmniLoader;
import kotlin.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polyfrost.oneconfig.api.config.v1.internal.ConfigVisualizer;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.polyfrost.oneconfig.api.event.v1.events.InitializationEvent;
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent;
import org.polyfrost.oneconfig.api.event.v1.events.WindowFocusEvent;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils;
import org.polyfrost.oneconfig.api.platform.v1.Platform;
import org.polyfrost.oneconfig.api.ui.v1.UIManager;
import org.polyfrost.oneconfig.api.ui.v1.internal.BlurHandler;
import org.polyfrost.oneconfig.api.ui.v1.keybind.OCKeybindHelper;
import org.polyfrost.oneconfig.internal.ui.OneConfigUI;
import org.polyfrost.oneconfig.utils.v1.MavenUpdateChecker;
import org.polyfrost.oneconfig.utils.v1.Multithreading;
import org.polyfrost.polyui.PolyUI;
import org.polyfrost.polyui.component.Drawable;
import org.polyfrost.polyui.input.KeyModifiers;
import org.polyfrost.polyui.input.Translator;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main class of OneConfig.
 */
//#if FORGE-LIKE
//#if MC <= 1.12.2
@net.minecraftforge.fml.common.Mod(modid = "oneconfig")
//#else
//#if NEOFORGE
//$$ @net.neoforged.fml.common.Mod("oneconfig")
//#else
//$$ @net.minecraftforge.fml.common.Mod("oneconfig")
//#endif
//#endif
//#endif
public class OneConfig
        //#if FABRIC
        //$$ implements net.fabricmc.api.ClientModInitializer
        //#endif
{
    public static final OneConfig INSTANCE = new OneConfig();
    private static final Logger LOGGER = LogManager.getLogger("OneConfig");

    //#if FORGE-LIKE
    //#if MC <= 1.12.2
    @net.minecraftforge.fml.common.Mod.EventHandler
    private void onInit(net.minecraftforge.fml.common.event.FMLPostInitializationEvent ev) {
        init();
    }
    //#else
    //$$ static {
    //$$     INSTANCE.init();
    //$$ }
    //#endif
    //#else
    //$$ @Override
    //$$ public void onInitializeClient() {
    //$$     init();
    //$$ }
    //#endif


    private void init() {
        //#if FABRIC
        //$$ try {
        //$$     Class.forName("org.polyfrost.oneconfig.test.TestMod_Test", false, getClass().getClassLoader());
        //$$     Class<?> test = Class.forName("org.polyfrost.oneconfig.test.TestMod_Test");
        //$$     org.polyfrost.oneconfig.utils.v1.MHUtils.getMethodHandle(test, "onInitializeClient", void.class).getOrThrow().invoke(test.getConstructor().newInstance());
        //$$ } catch (Throwable ignored) {
        //$$ }
        //#endif
        OmniLoader.ModInfo self = OmniLoader.getModInfo("oneconfig");
        String v = self == null ? "LOCAL" : self.getVersion();
        LOGGER.info("Loading OneConfig v{}", v);
        OmniClientCommands.initialize();
        BlurHandler.init();

        preloadCopycat();
        preloadPolyUI();

        registerCommands();
        registerKeybinds();
        registerEventHandlers();

        LOGGER.info("OneConfig initialized!");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerCommands() {
        LiteralArgumentBuilder b = OmniClientCommands.literal("oneconfig");
        b.executes(cmd -> {
            OneConfigUI.INSTANCE.open();
            return 1;
        });
        b.then(OmniClientCommands.literal("updateCheck").executes(cmd -> {
            Multithreading.submit(() -> OmniChat.showChatMessage(MavenUpdateChecker.oneconfig().hasUpdate() ? "Update available!" : "No updates available"));
            return 1;
        }));
        b.then(OmniClientCommands.literal("locraw").executes(cmd -> {
            OmniChat.showChatMessage(HypixelUtils.getLocation().toString());
            return 1;
        }));
        b.then(OmniClientCommands.literal("hud").executes(cmd -> {
            Platform.screen().display(HudManager.INSTANCE.getWithEditor());
            return 1;
        }));
        b.then(OmniClientCommands.literal("delete").executes(cmd -> {
            OneConfigUI.INSTANCE.invalidateCache();
            ConfigVisualizer.INSTANCE.clearCache();
            OmniChat.showChatMessage("Deleted OneConfig UI. Please make a report if you were having issues!");
            return 1;
        }));

        CommandNode node = b.build();
        OmniClientCommands.register(b);
        OmniClientCommands.register(OmniClientCommands.literal("ocfg").redirect(node));
        OmniClientCommands.register(OmniClientCommands.literal("twoconfig").redirect(node));
    }

    private static void registerKeybinds() {
        OCKeybindHelper builder = OCKeybindHelper.builder();
        if (OmniLoader.isDevelopment()) builder.inScreens();
        builder.mods(KeyModifiers.RSHIFT).does((s) -> {
            if (s) {
                try {
                    OneConfigUI.INSTANCE.open();
                } catch (Throwable t) {
                    OmniChat.showChatMessage("&cFailed to open OneConfig UI: " + t.getMessage() + ". Please report this!");
                    // propagate for proper error handling
                    throw t;
                }
            }
            return Unit.INSTANCE;
        });

        builder.register();
    }

    private static void registerEventHandlers() {
        EventManager.register(InitializationEvent.class, e -> HudManager.INSTANCE.initialize());
        //#if MC < 1.13
        // this is cringe but is better than the alternative of checking every frame in a mixin (that's how vanilla does it lol)
        AtomicBoolean active = new AtomicBoolean(false);
        EventManager.register(TickEvent.End.class, e -> {
            boolean current = org.lwjgl.opengl.Display.isActive();
            if (current != active.get()) {
                active.set(current);
                if (current) EventManager.INSTANCE.post(WindowFocusEvent.Gained.INSTANCE);
                else EventManager.INSTANCE.post(WindowFocusEvent.Lost.INSTANCE);
            }
        });
        //#endif
    }

    /**
     * Ensure that key PolyUI classes are loaded to prevent lag-spikes when loading PolyUI for the first time.
     */
    private static void preloadPolyUI() {
        long t1 = System.nanoTime();
        try {
            // PolyUI
            Class.forName(PolyUI.class.getName());
            Class.forName(Drawable.class.getName());
            Class.forName(Translator.class.getName());

            // OneConfig PolyUI renderer
            // todo: fix for fabric loaders as fails due to running too early
            //#if FORGE
            UIManager.INSTANCE.getRenderer();
            //#endif
        } catch (Exception e) {
            throw new IllegalStateException("Failed to preload necessary PolyUI classes", e);
        }

        LOGGER.info("PolyUI preload took {}ms", (System.nanoTime() - t1) / 1_000_000.0);
    }

    private static void preloadCopycat() {
        long t1 = System.nanoTime();
        try {
            // Copycat
            Clipboard.getInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to preload necessary Copycat classes", e);
        }

        LOGGER.info("Copycat preload took {}ms", (System.nanoTime() - t1) / 1_000_000.0);
    }
}

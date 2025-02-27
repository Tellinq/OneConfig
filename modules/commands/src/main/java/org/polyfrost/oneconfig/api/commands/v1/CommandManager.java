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

package org.polyfrost.oneconfig.api.commands.v1;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.deftu.omnicore.client.OmniClientCommands;
import org.polyfrost.oneconfig.api.commands.v1.factories.CommandFactory;
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.AnnotationCommandFactory;
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command;

import java.util.*;

/**
 * Handles the registration of OneConfig commands.
 *
 * @see Command
 */
public class CommandManager {
    /**
     * The singleton instance of the command manager.
     */
    public static final CommandManager INSTANCE = new CommandManager();
    private final Set<CommandFactory> factories = new HashSet<>();
    private final Map<Class<?>, ArgumentType<?>> argumentTypes = new IdentityHashMap<>();


    private CommandManager() {
        registerFactory(new AnnotationCommandFactory());
        argumentTypes.put(int.class, IntegerArgumentType.integer());
        argumentTypes.put(String.class, StringArgumentType.string());
        argumentTypes.put(boolean.class, BoolArgumentType.bool());
        argumentTypes.put(float.class, FloatArgumentType.floatArg());
        argumentTypes.put(double.class, DoubleArgumentType.doubleArg());
        argumentTypes.put(long.class, LongArgumentType.longArg());
    }

    public static void registerCommand(LiteralArgumentBuilder<ClientCommandSource> builder) {
        OmniClientCommands.INSTANCE.register(builder);
    }

    public static void registerCommand(LiteralCommandNode<ClientCommandSource> node) {
        OmniClientCommands.INSTANCE.register(node);
    }

    public static boolean register(Object obj) {
        LiteralCommandNode<ClientCommandSource>[] nodes = INSTANCE.create(obj);
        if (nodes == null) return false;
        for (LiteralCommandNode<ClientCommandSource> node : nodes) {
            registerCommand(node);
        }
        return true;
    }

    public LiteralCommandNode<ClientCommandSource>[] create(Object obj) {
        for (CommandFactory factory : factories) {
            LiteralCommandNode<ClientCommandSource>[] nodes = factory.create(obj);
            if (nodes != null) return nodes;
        }
        return null;
    }

    public static LiteralArgumentBuilder<ClientCommandSource> literal(String name) {
        return OmniClientCommands.INSTANCE.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return OmniClientCommands.INSTANCE.argument(name, type);
    }

    /**
     * Register a factory which can be used to create commands from objects in the {@link #create(Object)} method.
     */
    public void registerFactory(CommandFactory factory) {
        factories.add(factory);
    }

    public void registerArgumentType(Class<?> type, ArgumentType<?> argumentType) {
        argumentTypes.put(type, argumentType);
    }

    @SuppressWarnings("unchecked")
    public static <T> ArgumentType<T> getArgumentType(Class<T> type) {
        return (ArgumentType<T>) INSTANCE.argumentTypes.get(type);
    }
}

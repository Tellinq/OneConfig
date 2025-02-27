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

package org.polyfrost.oneconfig.api.commands.v1.factories.annotated;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.polyfrost.oneconfig.api.commands.v1.ClientCommandSource;
import org.polyfrost.oneconfig.api.commands.v1.CommandManager;
import org.polyfrost.oneconfig.api.commands.v1.factories.CommandFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class AnnotationCommandFactory implements CommandFactory {

    private static void create(LiteralArgumentBuilder<ClientCommandSource> tree, Object it) {
        for (Method m : it.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class)) {
                // todo
                LiteralArgumentBuilder<ClientCommandSource> methodBuilder = null;

                String[] paramNames = new String[m.getParameterCount()];
                Class<?>[] paramTypes = m.getParameterTypes();
                Parameter[] params = m.getParameters();
                for (int i = 0; i < paramNames.length; i++) {
                    paramNames[i] = params[i].getName();
                    methodBuilder = methodBuilder.then(
                            CommandManager.argument(paramNames[i], CommandManager.getArgumentType(paramTypes[i]))
                    );
                }

                methodBuilder.executes((ctx) -> {
                    Object[] args = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        args[i] = ctx.getArgument(paramNames[i], paramTypes[i]);
                    }
                    return m.invoke(it, args);
                });
                tree.then(methodBuilder.build());
            }
        }
        for (Class<?> cls : it.getClass().getDeclaredClasses()) {
            if (cls.isAnnotationPresent(Command.class)) {
                Command c = cls.getAnnotation(Command.class);
                LiteralArgumentBuilder<ClientCommandSource> classBuilder = CommandManager.literal(c.value().length == 0 ? cls.getSimpleName() : c.value()[0]);
                LiteralCommandNode<ClientCommandSource> classNode = classBuilder.build();
                tree.then(classBuilder.build());
            }
        }
    }


    @Override
    public LiteralCommandNode<ClientCommandSource>[] create(@NotNull Object obj) {
        Command c = obj.getClass().getAnnotation(Command.class);
        if (c == null) return null;
        LiteralArgumentBuilder<ClientCommandSource> builder = CommandManager.literal(c.value().length == 0 ? obj.getClass().getSimpleName() : c.value()[0]);
        LiteralCommandNode<ClientCommandSource>[] nodes = new LiteralCommandNode[Math.max(1, c.value().length)];
        nodes[0] = builder.build();
        for (int i = 1; i < c.value().length; i++) {
            nodes[i] = CommandManager.literal(c.value()[i]).redirect(builder).build();
        }
        return nodes;
    }
}

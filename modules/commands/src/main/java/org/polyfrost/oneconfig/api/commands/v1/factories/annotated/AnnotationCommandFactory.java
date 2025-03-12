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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.deftu.omnicore.client.OmniClientCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.polyfrost.oneconfig.api.commands.v1.factories.CommandFactory;
import org.polyfrost.oneconfig.utils.v1.MHUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.polyfrost.oneconfig.api.commands.v1.CommandManager.*;

public class AnnotationCommandFactory implements CommandFactory {
    private static final Logger LOGGER = LogManager.getLogger("OneConfig/BrigaiderTranslator");

    private void create(LiteralArgumentBuilder<OmniClientCommandSource> tree, Object it, StringBuilder help, String stem) {


        for (Method m : it.getClass().getDeclaredMethods()) {
            Command cmd = m.getAnnotation(Command.class);
            if (cmd != null) {
                help.append(stem).append(' ').append(cmd.value().length == 0 ? m.getName() : cmd.value()[0]);

                ArgumentBuilder<OmniClientCommandSource, ?> theMethod;
                if(m.getParameterCount() > 0) {
                    help.append('<');
                    String[] paramNames = new String[m.getParameterCount()];
                    Class<?>[] paramTypes = m.getParameterTypes();
                    Parameter[] params = m.getParameters();
                    theMethod = argument(paramNames[0], getArgumentType(paramTypes[0]));
                    help.append(paramNames[0]);
                    org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Parameter annotation = params[0].getAnnotation(org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Parameter.class);
                    if (annotation != null) {
                        help.append(": ").append(annotation.value());
                    }
                    for (int i = 1; i < paramNames.length; i++) {
                        Parameter p = params[i];
                        String name = p.getName();
                        help.append(", ").append(name);
                        annotation = p.getAnnotation(org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Parameter.class);
                        if (annotation != null) {
                            help.append(": ").append(annotation.value());
                        }
                        paramNames[i] = name;
                        theMethod = theMethod.then(
                                argument(paramNames[i], getArgumentType(paramTypes[i]))
                        );
                    }
                    help.append('>');
                    theMethod.executes((ctx) -> {
                        Object[] args = new Object[paramTypes.length];
                        for (int i = 0; i < paramTypes.length; i++) {
                            args[i] = ctx.getArgument(paramNames[i], paramTypes[i]);
                        }
                        try {
                            m.invoke(it, args);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().showError("An error occurred while executing this command!\nPlease report this to the developer: " + e.getMessage());
                            LOGGER.error("Failed to execute command!", e);
                            return -1;
                        }
                    });
                    tree.then(literal(m.getName()).then(theMethod));
                    for (String s : cmd.value()) {
                        tree.then(literal(s).then(theMethod));
                    }
                } else {
                    theMethod = literal(m.getName());
                    theMethod.executes((ctx) -> {
                        try {
                            m.invoke(it);
                            return 1;
                        } catch (Exception e) {
                            ctx.getSource().showError("An error occurred while executing this command!\nPlease report this to the developer: " + e.getMessage());
                            LOGGER.error("Failed to execute command!", e);
                            return -1;
                        }
                    });
                    tree.then(theMethod);
                    for (String s : cmd.value()) {
                        tree.then(literal(s).then(theMethod));
                    }
                }
                help.append(": ").append(cmd.description()).append('\n');
            }
        }
        for (Class<?> cls : it.getClass().getDeclaredClasses()) {
            Command sub = cls.getAnnotation(Command.class);
            if (sub != null) {
                LiteralArgumentBuilder<OmniClientCommandSource> subTree = literal(cls.getName());
                create(subTree, MHUtils.instantiate(cls, true), help, stem + " " + (sub.value().length == 0 ? cls.getName() : sub.value()[0]));
            }
        }
    }


    @Override
    public LiteralCommandNode<OmniClientCommandSource>[] create(@NotNull Object obj) {
        Class<?> cls = obj.getClass();
        Command c = cls.getAnnotation(Command.class);
        if (c == null) return null;
        StringBuilder help = new StringBuilder();
        String primaryName = c.value().length == 0 ? cls.getName() : c.value()[0];
        help.append("Help for /").append(primaryName).append(':');
        if (c.value().length != 0) {
            help.append("  (");
            for (String s : c.value()) {
                help.append(s).append(',');
            }
            help.setCharAt(help.length() - 1, ')');
        }
        help.append('\n');

        LiteralArgumentBuilder<OmniClientCommandSource> builder = literal(cls.getName());
        create(builder, obj, help, "  /" + primaryName);
        LiteralCommandNode<OmniClientCommandSource>[] nodes = new LiteralCommandNode[Math.max(1, c.value().length + 1)];
        builder.then(literal("help").executes((ctx) -> {
            ctx.getSource().showMessage(help.toString());
            return 1;
        }));
        nodes[0] = builder.build();
        for (int i = 1; i < c.value().length; i++) {
            nodes[i] = literal(c.value()[i]).redirect(builder.build()).build();
        }
        return nodes;
    }
}

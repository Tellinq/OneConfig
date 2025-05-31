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

package org.polyfrost.oneconfig.test;

import com.mojang.authlib.GameProfile;
import dev.deftu.omnicore.client.OmniChat;
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command;
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Executor;

@Command(value = {"test", "t"})
public class TestCommand_Test {

    @Executor
    private static void main() {  // /test
        OmniChat.displayClientMessage("Main command");
    }

    private static void joinAndChat(String... stuff) {
        StringBuilder builder = new StringBuilder();
        for (Object thing : stuff) {
            builder.append(thing).append(' ');
        }
        OmniChat.displayClientMessage(builder.toString().trim());
    }

    @Executor
    private void playerTest(GameProfile profile) {
        OmniChat.displayClientMessage("Player test: " + profile.getName());
        OmniChat.displayClientMessage(profile.getId().toString());
    }

    @Command(value = {"subcommand", "s"})
    private static class TestSubCommand {
        private static void main(int a, float b, String c) { // /test subcommand <a> <b> <c>
            OmniChat.displayClientMessage("Integer main: " + (a + b) + " " + c);
        }

        @Executor(value = {"yesNo"})
        private void yes(double a, double b, String c) { // /test subcommand <a> <b> <c>
            OmniChat.displayClientMessage("Double main: " + a + " " + b + " " + c);
        }

        @Command(value = {"subSub", "ss"})
        private static class TestSubSubCommand {
            private void wow(int a, float b, String c) { // /test subSub <a> <b> <c>
                OmniChat.displayClientMessage("Integer subSub: " + (a + b) + " " + c);
            }
        }
    }
}

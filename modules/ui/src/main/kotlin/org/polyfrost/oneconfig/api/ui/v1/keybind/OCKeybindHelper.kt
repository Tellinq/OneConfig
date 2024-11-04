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

package org.polyfrost.oneconfig.api.ui.v1.keybind

import org.polyfrost.polyui.input.KeyBinder
import org.polyfrost.polyui.input.KeybindHelper
import org.polyfrost.polyui.utils.nullIfEmpty

/**
 * Java builder-style helper for creating keybinds.
 */
class OCKeybindHelper : KeybindHelper() {
    private var inScreens = false

    override fun build(): KeyBinder.Bind {
        val func = func ?: throw IllegalStateException("Function must be set")
        return if (!inScreens) BindNotInScreen(
            unmappedKeys.nullIfEmpty()?.toIntArray(),
            keys.nullIfEmpty()?.toTypedArray(),
            mouse.nullIfEmpty()?.toIntArray(),
            mods, duration, func
        ) else super.build()
    }

    fun register() = build().register()

    fun KeyBinder.Bind.register() = KeybindManager.registerKeybind(this)

    companion object {
        @JvmStatic
        fun builder() = OCKeybindHelper()
    }
}
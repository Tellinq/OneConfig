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

import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.KeyInputEvent
import org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.event.v1.events.WindowFocusEvent
import org.polyfrost.polyui.Settings
import org.polyfrost.polyui.input.InputManager
import org.polyfrost.polyui.input.KeyBinder
import org.polyfrost.polyui.input.KeyModifiers
import org.polyfrost.polyui.input.Keys
import org.polyfrost.polyui.utils.Int2IntMap
import org.polyfrost.universal.UKeyboard

@Suppress("UnstableApiUsage")
object KeybindManager {
    private val LOGGER = LogManager.getLogger("OneConfig/Keybinds")
    private val settings = Settings()
    private val keyBinder = KeyBinder(settings)
    val inputManager = InputManager(null, keyBinder, settings)

    @JvmStatic
    val modsMap: Int2IntMap

    @JvmStatic
    val keysMap: Map<Int, Keys>


    init {
        eventHandler { (key, char, state): KeyInputEvent ->
            if (state == 2) return@eventHandler
            translateKey(inputManager, key, char, state == 1)
        }
        eventHandler { _: TickEvent.End ->
            keyBinder.update(50_000L, inputManager.mods, true)
        }

        // asm: this is an old fix which will be kept so that in the (rare) event that the keybind system fails for whatever reason,
        // the user can try to fix it by opening a screen and trying again, and it should fix the issue.
        eventHandler { (screen): ScreenOpenEvent ->
            if (screen == null) keyBinder.release()
        }
        eventHandler { _: WindowFocusEvent.Lost ->
            keyBinder.release()
        }

        val m = Int2IntMap(8)
        m[UKeyboard.KEY_LSHIFT] = KeyModifiers.LSHIFT.value.toInt()
        m[UKeyboard.KEY_RSHIFT] = KeyModifiers.RSHIFT.value.toInt()
        m[UKeyboard.KEY_LCONTROL] = KeyModifiers.LCONTROL.value.toInt()
        m[UKeyboard.KEY_RCONTROL] = KeyModifiers.RCONTROL.value.toInt()
        m[UKeyboard.KEY_LMENU] = KeyModifiers.LALT.value.toInt()
        m[UKeyboard.KEY_RMENU] = KeyModifiers.RALT.value.toInt()
        m[UKeyboard.KEY_LMETA] = KeyModifiers.LMETA.value.toInt()
        m[UKeyboard.KEY_RMETA] = KeyModifiers.RMETA.value.toInt()
        modsMap = m

        keysMap = hashMapOf(
            UKeyboard.KEY_F1 to Keys.F1,
            UKeyboard.KEY_F2 to Keys.F2,
            UKeyboard.KEY_F3 to Keys.F3,
            UKeyboard.KEY_F4 to Keys.F4,
            UKeyboard.KEY_F5 to Keys.F5,
            UKeyboard.KEY_F6 to Keys.F6,
            UKeyboard.KEY_F7 to Keys.F7,
            UKeyboard.KEY_F8 to Keys.F8,
            UKeyboard.KEY_F9 to Keys.F9,
            UKeyboard.KEY_F10 to Keys.F10,
            UKeyboard.KEY_F11 to Keys.F11,
            UKeyboard.KEY_F12 to Keys.F12,

            UKeyboard.KEY_ESCAPE to Keys.ESCAPE,
            UKeyboard.KEY_BACKSPACE to Keys.BACKSPACE,
            UKeyboard.KEY_TAB to Keys.TAB,
            UKeyboard.KEY_ENTER to Keys.ENTER,
            UKeyboard.KEY_END to Keys.END,
            UKeyboard.KEY_HOME to Keys.HOME,

            UKeyboard.KEY_LEFT to Keys.LEFT,
            UKeyboard.KEY_UP to Keys.UP,
            UKeyboard.KEY_RIGHT to Keys.RIGHT,
            UKeyboard.KEY_DOWN to Keys.DOWN
        )

    }

    @JvmStatic
    fun registerKeybind(bind: KeyBinder.Bind): KeyBinder.Bind {
        keyBinder.add(bind)
        return bind
    }

    @JvmStatic
    fun builder() = OCKeybindHelper()

    @JvmStatic
    fun translateKey(inputManager: InputManager, key: Int, char: Char, state: Boolean) {
        // fix for modified characters not being sent in glfwCharCallback as glfwSetCharModsCallback is deprecated
        // for more info (see PolyUI/nanovg-impl/GLFWWindow)
        val character = if (!char.isValid() && key < 255 && inputManager.mods > 1.toByte() && state) (key + 32).toChar() else char
        try {
            if (character.isValid()) {
                if (state) {
                    inputManager.keyTyped(character)
                    inputManager.keyDown(character.lowercaseChar().code)
                } else inputManager.keyUp(character.lowercaseChar().code)
                return
            }

            val k = keysMap[key]
            if (k != null) {
                if (state) inputManager.keyDown(k)
                else inputManager.keyUp(k)
                return
            }

            val m = modsMap[key].toByte()
            if (m != 0.toByte()) {
                if (state) inputManager.addModifier(m)
                else inputManager.removeModifier(m)
                return
            }

            val raw = if (inputManager.mods > 1) key + 48 else key
            if (state) inputManager.keyDown(raw)
            else inputManager.keyUp(raw)
        } catch (t: Throwable) {
            LOGGER.error("Failed to process input key=$key, char=$character, state=$state", t)
        }
    }

    private fun Char.isValid() = this != '\u0000' && !this.isISOControl() && this.isDefined()
}
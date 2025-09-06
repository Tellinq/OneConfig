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

import dev.deftu.omnicore.client.OmniKeyboard
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.KeyInputEvent
import org.polyfrost.oneconfig.api.event.v1.events.ScreenOpenEvent
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.event.v1.events.WindowFocusEvent
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.Settings
import org.polyfrost.polyui.input.InputManager
import org.polyfrost.polyui.input.KeyBinder
import org.polyfrost.polyui.input.KeyModifiers
import org.polyfrost.polyui.input.Keys

@Suppress("UnstableApiUsage")
object KeybindManager {
    private val LOGGER = LogManager.getLogger("OneConfig/Keybinds")
    private val settings = Settings()
    private val keyBinder = KeyBinder(settings)
    val inputManager = InputManager(null, keyBinder, settings)


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
            if (screen == null) {
                inputManager.drop()
                keyBinder.release()
            }
        }
        eventHandler { _: WindowFocusEvent.Lost ->
            // clear all modifiers
            inputManager.drop()
            keyBinder.release()
        }
    }

    @JvmStatic
    fun registerKeybind(bind: KeyBinder.Bind?): KeyBinder.Bind? {
        if (bind != null) keyBinder.add(bind)
        return bind
    }

    @JvmStatic
    fun builder() = OCKeybindHelper()

    @JvmStatic
    fun translateKey(inputManager: InputManager, keyCode: Int, char: Char, down: Boolean) {
        try {
            if (char != '\u0000' && char.isValid()) {
                if (down) inputManager.keyTyped(char.toInt())
            }
            val mod = when (keyCode) {
                OmniKeyboard.KEY_LSHIFT -> KeyModifiers.LSHIFT
                OmniKeyboard.KEY_LCONTROL -> if (PolyUI.isOnMac) KeyModifiers.LMETA else KeyModifiers.LPRIMARY
                OmniKeyboard.KEY_LMENU -> KeyModifiers.LSECONDARY
                OmniKeyboard.KEY_LMETA -> if (PolyUI.isOnMac) KeyModifiers.LPRIMARY else KeyModifiers.LSECONDARY
                OmniKeyboard.KEY_RSHIFT -> KeyModifiers.RSHIFT
                OmniKeyboard.KEY_RCONTROL -> if (PolyUI.isOnMac) KeyModifiers.RMETA else KeyModifiers.RPRIMARY
                OmniKeyboard.KEY_RMENU -> KeyModifiers.RSECONDARY
                OmniKeyboard.KEY_RMETA -> if (PolyUI.isOnMac) KeyModifiers.RPRIMARY else KeyModifiers.RSECONDARY
                else -> null
            }

            if (mod != null) {
                if (down) {
                    inputManager.addModifier(mod.value)
                } else {
                    inputManager.removeModifier(mod.value)
                }
                return
            }


            val key = when (keyCode) {
                OmniKeyboard.KEY_F1 -> Keys.F1
                OmniKeyboard.KEY_F2 -> Keys.F2
                OmniKeyboard.KEY_F3 -> Keys.F3
                OmniKeyboard.KEY_F4 -> Keys.F4
                OmniKeyboard.KEY_F5 -> Keys.F5
                OmniKeyboard.KEY_F6 -> Keys.F6
                OmniKeyboard.KEY_F7 -> Keys.F7
                OmniKeyboard.KEY_F8 -> Keys.F8
                OmniKeyboard.KEY_F9 -> Keys.F9
                OmniKeyboard.KEY_F10 -> Keys.F10
                OmniKeyboard.KEY_F11 -> Keys.F11
                OmniKeyboard.KEY_F12 -> Keys.F12

                OmniKeyboard.KEY_ESCAPE -> Keys.ESCAPE

                OmniKeyboard.KEY_ENTER -> Keys.ENTER
                OmniKeyboard.KEY_TAB -> Keys.TAB
                OmniKeyboard.KEY_BACKSPACE -> Keys.BACKSPACE
                OmniKeyboard.KEY_DELETE -> Keys.DELETE
                OmniKeyboard.KEY_HOME -> Keys.HOME
                OmniKeyboard.KEY_END -> Keys.END

                OmniKeyboard.KEY_RIGHT -> Keys.RIGHT
                OmniKeyboard.KEY_LEFT -> Keys.LEFT
                OmniKeyboard.KEY_DOWN -> Keys.DOWN
                OmniKeyboard.KEY_UP -> Keys.UP

                OmniKeyboard.KEY_C -> Keys.C
                OmniKeyboard.KEY_V -> Keys.V
                OmniKeyboard.KEY_X -> Keys.X
                OmniKeyboard.KEY_Z -> Keys.Z
                OmniKeyboard.KEY_A -> Keys.A
                OmniKeyboard.KEY_S -> Keys.S
                OmniKeyboard.KEY_P -> Keys.P
                OmniKeyboard.KEY_I -> Keys.I
                OmniKeyboard.KEY_R -> Keys.R
                OmniKeyboard.KEY_MINUS -> Keys.MINUS
                OmniKeyboard.KEY_EQUALS -> Keys.EQUALS

                else -> Keys.UNKNOWN
            }
            if (key != Keys.UNKNOWN) {
                if (down) inputManager.keyDown(key)
                else inputManager.keyUp(key)
            }
            if (down) inputManager.keyDown(keyCode, -1)
            else inputManager.keyUp(keyCode, -1)
        } catch (t: Throwable) {
            LOGGER.error("Failed to process input key=$keyCode, char=$char, down=$down", t)
        }
    }

    private fun Char.isValid() = this != '\u0000' && !this.isISOControl() && this.isDefined()
}
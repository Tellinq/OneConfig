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

package org.polyfrost.oneconfig.api.event.v1.events;

/**
 * Called when a sound is played.
 * <br>
 * Due to Minecraft versioning, the sound object can be of different types. it is safe to cast this to the appropriate type for your version (e.g. ISound for 1.8.9)
 */
public class SoundPlayEvent extends Event.Cancellable {
    private final Object sound;

    public SoundPlayEvent(Object sound) {
        this.sound = sound;
    }

    public <T> T component1() {
        return getSound();
    }

    /**
     * Due to differences across Minecraft versions, this is a Duck method, meaning that it will return the expected type for that minecraft version.
     * <ul>
     *     <li>For modern forge, this will be a ClientLevel.</li>
     *     <li>For fabric & forge pre-1.17, this will be a ClientWorld.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public <T> T getSound() {
        return (T) sound;
    }
}

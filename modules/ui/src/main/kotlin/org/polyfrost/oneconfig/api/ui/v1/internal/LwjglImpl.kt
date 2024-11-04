package org.polyfrost.oneconfig.api.ui.v1.internal

import org.lwjgl.system.MemoryUtil
import org.polyfrost.oneconfig.api.ui.v1.api.LwjglApi

class LwjglImpl : LwjglApi {
    override fun memAlloc(size: Int) = MemoryUtil.memAlloc(size)
}

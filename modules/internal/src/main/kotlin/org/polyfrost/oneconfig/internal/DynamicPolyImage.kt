package org.polyfrost.oneconfig.internal

import org.polyfrost.polyui.data.PolyImage
import java.io.InputStream

class DynamicPolyImage @JvmOverloads constructor(
    resourcePath: String,
    private val stream: InputStream,
    type: Type = from(resourcePath),
) : PolyImage(resourcePath, type) {

    override fun stream(): InputStream {
        return stream
    }

}

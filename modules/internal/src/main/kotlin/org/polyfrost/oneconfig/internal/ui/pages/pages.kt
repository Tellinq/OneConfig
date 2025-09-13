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

package org.polyfrost.oneconfig.internal.ui.pages

import org.polyfrost.oneconfig.internal.ui.OneConfigUI
import org.polyfrost.polyui.color.Colors
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.component.extensions.denyPaletteChanges
import org.polyfrost.polyui.component.extensions.ignoreLayout
import org.polyfrost.polyui.component.extensions.named
import org.polyfrost.polyui.component.extensions.onClick
import org.polyfrost.polyui.component.extensions.padded
import org.polyfrost.polyui.component.extensions.radius
import org.polyfrost.polyui.component.extensions.setFont
import org.polyfrost.polyui.component.extensions.setPalette
import org.polyfrost.polyui.component.extensions.withBorder
import org.polyfrost.polyui.component.extensions.withHoverStates
import org.polyfrost.polyui.component.impl.Block
import org.polyfrost.polyui.component.impl.Group
import org.polyfrost.polyui.component.impl.Image
import org.polyfrost.polyui.component.impl.Text
import org.polyfrost.polyui.data.PolyImage
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.mapToArray
import org.polyfrost.polyui.utils.translated

fun ThemesPage(vararg registeredThemes: Colors): Drawable {
    return Group(
        ThemesLargeCard(),
        Group(
            *registeredThemes.mapToArray { ThemesSmallCard(it) },
            alignment = Align(padEdges = Vec2(16f, 0f), padBetween = Vec2(16f, 16f)),
            visibleSize = Vec2(750f, 173f)
        ),
        visibleSize = Vec2(1130f, 635f),
        alignment = Align(padEdges = Vec2(18f, 18f))
    ).named("oneconfig.themes")
}

private fun ThemesLargeCard() = Block(
    Block(
        Block(size = Vec2(108f, 5f)).setPalette { text.primary },
        Block(size = Vec2(48f, 3f)).padded(0f, -1f, 0f, 9f).setPalette { text.secondary},
        Block(size = Vec2(84f, 4f)).setPalette { state.danger },
        Block(size = Vec2(60f, 4f)).setPalette { state.warning },
        Block(size = Vec2(78f, 4f)).setPalette { state.success },
        Block(at = Vec2(96f, 99f), size = Vec2(64f, 16f)).radius(4f).ignoreLayout().setPalette { onBrand.accent },
        Block(at = Vec2(96f, 99f), size = Vec2(32f, 16f), radii = floatArrayOf(4f, 0f, 4f, 0f)).ignoreLayout().setPalette { brand.fg },
        alignment = Align(padEdges = Vec2(20f, 20f), padBetween = Vec2(8f, 8f), wrap = Align.Wrap.NEVER, mode = Align.Mode.Vertical, line = Align.Line.Start),
        size = Vec2(168f, 123f)
    ).withBorder(1f).padded(112f, 44f, 16f, 16f),
    alignment = Align(pad = Vec2.ZERO, wrap = Align.Wrap.NEVER, mode = Align.Mode.Vertical, line = Align.Line.Start),
    size = Vec2(296f, 183f)
).setPalette { page.bg }.withBorder(3f)

private fun ThemesSmallCard(colors: Colors) = Block(
    Block(size = Vec2(108f, 5f)).setPalette(colors.text.primary).denyPaletteChanges(),
    Block(size = Vec2(48f, 3f)).setPalette(colors.text.secondary).denyPaletteChanges(),
    Block(at = Vec2(54f, 54f), size = Vec2(64f, 16f)).radius(4f).ignoreLayout().setPalette(colors.onBrand.accent).denyPaletteChanges(),
    Block(at = Vec2(54f, 54f), size = Vec2(32f, 16f), radii = floatArrayOf(4f, 0f, 4f, 0f)).ignoreLayout().setPalette(colors.brand.fg).denyPaletteChanges(),
    size = Vec2(126f, 78f),
    alignment = Align(padEdges = Vec2(8f, 16f), padBetween = Vec2(8f, 6f), wrap = Align.Wrap.NEVER, mode = Align.Mode.Vertical, line = Align.Line.Start),
).setPalette(colors.page.bg).withBorder(1f).denyPaletteChanges().onClick {
    polyUI.colors = colors
}

fun FeedbackPage(): Drawable {
    return Group(
        Image(PolyImage("assets/oneconfig/brand/polyfrost.png")),
        Text("oneconfig.feedback.title", fontSize = 24f).setFont { medium },
        Text("oneconfig.feedback.credits", fontSize = 14f),
        Text("oneconfig.feedback.bugreport", fontSize = 24f).setFont { medium },
        Text("oneconfig.feedback.joindiscord", fontSize = 14f),
        size = Vec2(1130f, 0f),
        visibleSize = Vec2(1130f, 635f),
        alignment = Align(line = Align.Line.Start, mode = Align.Mode.Vertical, pad = Vec2(18f, 18f)),
    ).named("oneconfig.feedback")
}

fun ProfilesPage(): Drawable {
    return Group()
}

fun ChangelogPage(news: Collection<News>): Drawable {
    return Group(
        size = Vec2(1130f, 0f),
        visibleSize = Vec2(1130f, 635f),
        alignment = Align(line = Align.Line.Center, pad = Vec2(60f, 20f)),
        children = news.mapToArray {
            Group(
                if (it.image != null) Image(it.image) else null,
                Group(
                    Text(it.title, fontSize = 16f).setFont { medium },
                    Text(it.summary, visibleSize = Vec2(612f, 166f)),
                    Group(
                        Text(it.dateString),
                        Text("oneconfig.readmore").withHoverStates().onClick { _ ->
                            val page =
                                Group(
                                    if (it.image != null) Image(it.image) else null,
                                    Group(
                                        Text(it.title, fontSize = 24f).setFont { medium },
                                        Text("oneconfig.writtenby".translated(it.author)),
                                        Text(it.dateString),
                                    ),
                                    Text(it.content, fontSize = 14f, visibleSize = Vec2(1100f, 0f)),
                                    alignment = Align(line = Align.Line.Start),
                                    size = Vec2(1130f, 0f),
                                    visibleSize = Vec2(1130f, 635f),
                                ).named(it.title)
                            OneConfigUI.openPage(page)
                            // todo switch
                        },
                        size = Vec2(612f, 12f),
                        alignment = Align(main = Align.Content.SpaceBetween),
                    ),
                    alignment = Align(mode = Align.Mode.Vertical),
                ),
            )
        },
    ).named("oneconfig.changelog")
}

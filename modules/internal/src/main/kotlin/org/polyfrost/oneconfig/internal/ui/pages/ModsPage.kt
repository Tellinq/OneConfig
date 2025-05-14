package org.polyfrost.oneconfig.internal.ui.pages

import org.polyfrost.oneconfig.api.config.v1.ConfigManager
import org.polyfrost.oneconfig.api.config.v1.Tree
import org.polyfrost.oneconfig.api.config.v1.internal.ConfigVisualizer
import org.polyfrost.oneconfig.api.platform.v1.Platform
import org.polyfrost.oneconfig.api.ui.v1.Notifications
import org.polyfrost.oneconfig.internal.ui.OneConfigUI
import org.polyfrost.polyui.component.Drawable
import org.polyfrost.polyui.component.extensions.*
import org.polyfrost.polyui.component.impl.*
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.image

internal enum class TreeSource {
    CONFIG, // Comes from OneConfig's config manager
    COMMAND // Comes from another mod's command, which we assume opens it's config UI
}

internal fun ModsPage(trees: Map<TreeSource, Set<Tree>>): Drawable {
    if (trees.isEmpty()) {
        return Group(
            Text("oneconfig.mods.none", fontSize = 24f).setFont { medium },
            Text("oneconfig.mods.none.desc", fontSize = 14f),
            size = Vec2(1130f, 635f),
            alignment = Align(main = Align.Main.Center, pad = Vec2(18f, 18f), maxRowSize = 1),
        ).namedId("EmptyModsPage")
    }

    // todo add categories
    return Group(
        children = trees.flatMap { (source, treeSet) ->
            treeSet.mapNotNull { tree ->
                if (tree.getMetadata<Any?>("hidden") != null) {
                    println("Skipping hidden tree $tree")
                    return@mapNotNull null
                }

                Group(
                    Block(
                        Image(tree.getMetadata<String>("icon")?.image() ?: defaultModImage).onInit { size = size.coerceAtMost(
                            Vec2(64f, 64f)
                        ) },
                        radii = modBoxTopRad,
                        alignment = imageAlign,
                        size = Vec2(256f, 104f),
                    ).withStates(),
                    Block(
                        Text(tree.title, fontSize = 14f).setFont { medium },
                        Image(heart),
                        radii = modBoxBotRad,
                        alignment = barAlign,
                        size = Vec2(256f, 36f),
                    ).also { it.acceptsInput = true }.setPalette { brand.fg },
                    alignment = modBoxAlign,
                ).onClick { _ ->
                    when (source) {
                        TreeSource.CONFIG -> OneConfigUI.openPage(ConfigVisualizer.INSTANCE.get(tree), (this[1][0] as Text).text)
                        TreeSource.COMMAND -> Platform.compatibility().executeTreeAction(tree.id)
                    }
                }.onRightClick { _ ->
                    if (source == TreeSource.CONFIG) {
                        PopupMenu(Text("Restore Defaults").setDestructivePalette().withStates().onClick { _ ->
                            val backup = ConfigManager.backup().get(tree.id)
                            if (backup == null) {
                                Notifications.enqueue(
                                    Notifications.Type.Error,
                                    "Backup Failure",
                                    "Couldn't find the backup for ${tree.id}. You can fix this by manually deleting the config file and restarting your game, which will reset your config! Click here to do so."
                                ).onClick { _ ->
                                    ConfigManager.active().delete(tree.id)
                                }
                            }
                            tree.overwrite(backup)
                            polyUI.unfocus()
                            false
                        }, polyUI = polyUI)
                    }
                }.namedId("ModCard")
            }
        }.toTypedArray(),
        visibleSize = Vec2(1130f, 635f),
        alignment = Align(cross = Align.Cross.Start, pad = Vec2(18f, 18f)),
    ).makeRearrangeableGrid().namedId("ModsPage")
}

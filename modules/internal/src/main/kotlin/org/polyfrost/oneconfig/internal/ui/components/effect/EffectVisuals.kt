package org.polyfrost.oneconfig.internal.ui.components.effect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.picker.PickerLabel
import org.polyfrost.oneconfig.internal.ui.components.picker.PickerTileIcon
import org.polyfrost.oneconfig.internal.ui.components.picker.PickerTileSize
import org.polyfrost.oneconfig.internal.ui.components.picker.TilePicker
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/** The catalog deduplicated and sorted by display name so grid positions never jump */
@Composable
fun rememberEffectCatalog(key: Any? = Unit): List<EffectDescriptor> {
    val revision = EffectCatalog.revision
    return remember(key, revision) {
        EffectCatalog.effects()
            .distinctBy(EffectDescriptor::id)
            .sortedWith { first, second ->
                first.displayName.compareTo(second.displayName, ignoreCase = true)
                    .takeIf { it != 0 } ?: first.id.compareTo(second.id)
            }
    }
}

/** [rememberEffectCatalog] indexed by registry ID for resolving stored IDs to descriptors */
@Composable
fun rememberEffectCatalogById(catalog: List<EffectDescriptor>): Map<String, EffectDescriptor> =
    remember(catalog) { catalog.associateBy(EffectDescriptor::id) }

/** The icon for [id] showing [placeholder] if the effect is unknown */
@Composable
fun EffectIcon(
    id: String,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    placeholder: String = "eye-off",
) {
    val revision = EffectCatalog.revision
    val bitmap = remember(id, revision) { EffectCatalog.icon(id)?.toComposeImageBitmap() }
    if (bitmap == null) {
        Icon(
            placeholder,
            modifier = modifier.aspectRatio(1f).padding(2.dp),
            color = LocalTheme.current.textColorSecondary.copy(alpha = alpha),
        )
        return
    }
    Image(bitmap, contentDescription = null, modifier = modifier.aspectRatio(1f), alpha = alpha, filterQuality = FilterQuality.None)
}

/** Display name over registry ID where a `null` [effect] is not in the catalog so [id] becomes the name */
@Composable
fun EffectLabel(
    id: String,
    effect: EffectDescriptor?,
    modifier: Modifier = Modifier,
    nameColor: Color = LocalTheme.current.textColor,
    idColor: Color = LocalTheme.current.textColorSecondary,
    nameSize: TextUnit = 12.sp,
    idSize: TextUnit = 10.sp,
    unavailableKey: String = EffectStrings.Unavailable,
) = PickerLabel(id, effect, modifier, nameColor, idColor, nameSize, idSize, unavailableKey)

internal val EffectTileIcon: PickerTileIcon<EffectDescriptor> = { effect, modifier, alpha ->
    EffectIcon(effect.id, modifier, alpha)
}

/**
 * Header search field and result grid making up the whole effect selector for any HUD or screen
 *
 * The [modifier] supplies the chrome, minus [gutter] side padding, so it fits a dropdown a HUD editor panel
 * or a full screen
 *
 * [selected] and [onToggle] leave the selection with the caller
 *
 * [maxEntries] of `0` or less is unlimited and `1` is radio-style
 */
@Composable
fun EffectPicker(
    effects: List<EffectDescriptor>,
    selected: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxEntries: Int = 0,
    onClose: (() -> Unit)? = null,
    autoFocusSearch: Boolean = true,
    cellSize: Dp = PickerTileSize,
    gridMaxHeight: Dp = 300.dp,
    gutter: Dp = 12.dp,
    titleKey: String? = EffectStrings.Title,
    searchPlaceholderKey: String = EffectStrings.Search,
    emptyKey: String = EffectStrings.Empty,
    header: @Composable (ColumnScope.() -> Unit)? = null,
) = TilePicker(
    entries = effects,
    selected = selected,
    onToggle = onToggle,
    icon = EffectTileIcon,
    modifier = modifier,
    maxEntries = maxEntries,
    onClose = onClose,
    autoFocusSearch = autoFocusSearch,
    cellSize = cellSize,
    gridMaxHeight = gridMaxHeight,
    gutter = gutter,
    titleKey = titleKey,
    searchPlaceholderKey = searchPlaceholderKey,
    emptyKey = emptyKey,
    header = header,
)

/** Default translation keys where every composable taking one accepts an override */
object EffectStrings {
    const val Title = "oneconfig.effectlist.title"
    const val Search = "oneconfig.effectlist.search"
    const val Empty = "oneconfig.effectlist.empty"
    const val Unavailable = "oneconfig.effectlist.unavailable"
}

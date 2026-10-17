package org.polyfrost.oneconfig.internal.ui.components.settings

import androidx.compose.runtime.Composable
import org.polyfrost.oneconfig.internal.ui.api.settings.EffectListOptionData
import org.polyfrost.oneconfig.internal.ui.components.effect.EffectIcon
import org.polyfrost.oneconfig.internal.ui.components.effect.EffectStrings
import org.polyfrost.oneconfig.internal.ui.components.effect.EffectTileIcon
import org.polyfrost.oneconfig.internal.ui.components.effect.rememberEffectCatalog

@Composable
fun EffectListOption(data: EffectListOptionData) = PickerListOption(
    prop = data.prop,
    ids = data::ids,
    setIds = data::setIds,
    maxEntries = data.maxEntries,
    reorderable = data.reorderable,
    addText = data.addText,
    entries = rememberEffectCatalog(data.prop),
    icon = EffectTileIcon,
    rowIcon = { id, modifier -> EffectIcon(id, modifier) },
    titleKey = EffectStrings.Title,
    searchPlaceholderKey = EffectStrings.Search,
    emptyKey = EffectStrings.Empty,
    unavailableKey = EffectStrings.Unavailable,
)

package org.polyfrost.oneconfig.internal.ui.hud.screens.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.function.Consumer
import org.polyfrost.oneconfig.api.config.v1.Property
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudGroups
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.asRenderText
import org.polyfrost.oneconfig.internal.ui.components.localizedDescription
import org.polyfrost.oneconfig.internal.ui.components.localizedTitle
import org.polyfrost.oneconfig.internal.ui.components.settings.NumberSpinner
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.hud.HudSettingsContent
import org.polyfrost.oneconfig.internal.ui.hud.components.ChoiceDropdown
import org.polyfrost.oneconfig.internal.ui.hud.components.ChoiceRadio
import org.polyfrost.oneconfig.internal.ui.hud.repairHudStaticSize
import org.polyfrost.oneconfig.internal.ui.screens.HudOptionRow
import org.polyfrost.oneconfig.internal.ui.screens.rememberDisplay
import org.polyfrost.oneconfig.internal.ui.search.searchMatches
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

/** Counts edits so rows read the HUDs again after one, since plain HUD fields cannot tell compose they changed */
private class SharedEdits {
    private var count by mutableIntStateOf(0)

    val revision: Int get() = count

    fun bump() {
        count++
    }
}

private fun SharedValue<Boolean>.isOn(): Boolean = (this as? SharedValue.Single)?.value == true

private fun SharedValue<*>.isMixed(): Boolean = this is SharedValue.Mixed

/**
 * The settings every one of [huds] has, where an edit is written to all of them
 *
 * A setting the HUDs hold differently reads as mixed until it is changed, which sets it for every HUD
 *
 * [lockAndScale] leaves out the lock and the scale for a group, which has its own
 */
@Composable
fun SharedSettings(huds: Collection<Hud>, query: String, lockAndScale: Boolean = true) {
    val list = huds.toList()
    if (list.isEmpty()) return

    LaunchedEffect(list) { list.forEach(::repairHudStaticSize) }

    HudSettingsContent(list) {
        val edits = remember { SharedEdits() }
        if (lockAndScale) {
            if (query.isEmpty()) {
                Text(
                    "Changes apply to all ${list.size} selected HUDs. Settings that only some of them have are left out.",
                    color = LocalTheme.current.textColorSecondary,
                    fontSize = 12.sp,
                )
            }
            SharedLock(list, query)
            ScaleSection(list, query, locked = list.all { it.locked })
        }
        for (section in SharedSection.entries) {
            val options = SharedOptions.shared(section, list)
            if (options.isEmpty()) continue
            Section(section.title, query, *options.map { it.label }.toTypedArray()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    for (option in options) {
                        if (SharedOptions.isShown(option, list)) key(option.id) { SharedControl(option, list, edits) }
                    }
                }
            }
        }
        SharedPropertyRows(list, query, edits)
    }
}

@Composable
private fun SharedLock(huds: List<Hud>, query: String) {
    Section("Position", query, "Lock position") {
        val locked = resolveShared(huds) { it.locked }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SwitchControl(locked.isOn(), locked.isMixed()) { HudGroups.setLocked(huds, it) }
            Text("Lock position", color = LocalTheme.current.textColor, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SharedControl(option: SharedOption, huds: List<Hud>, edits: SharedEdits) {
    // read so the control reads the HUDs again after an edit
    edits.revision
    when (option) {
        is SharedToggle -> {
            val value = option.resolve(huds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SwitchControl(value.isOn(), value.isMixed()) {
                    option.apply(huds, it)
                    edits.bump()
                }
                Text(option.label, color = LocalTheme.current.textColor, fontSize = 14.sp)
            }
        }

        is SharedAmount -> {
            val value = option.resolve(huds)
            NumberSpinner(
                (value as? SharedValue.Single)?.value ?: option.read(huds.first()),
                {
                    option.apply(huds, it)
                    edits.bump()
                },
                option.min,
                option.max,
                option.step,
                width = option.widthDp.dp,
                unit = option.unit,
                label = option.label.takeIf { option.icon == null },
                icon = option.icon,
                mixed = value.isMixed(),
            )
        }

        is SharedColor -> {
            val first = huds.first()
            val color = (option.resolve(huds) as? SharedValue.Single)?.value ?: option.read(first)
            val chroma = (resolveShared(huds, option.readChroma) as? SharedValue.Single)?.value ?: option.readChroma(first)
            val speed = (resolveShared(huds, option.readSpeed) as? SharedValue.Single)?.value ?: option.readSpeed(first)
            ColorButton(
                option.label, Color(color),
                chroma = chroma, chromaSpeed = speed,
                onColorChanged = {
                    option.apply(huds, it.toArgb())
                    edits.bump()
                },
                onChromaChanged = { enabled, chromaSpeed ->
                    option.applyChroma(huds, enabled, chromaSpeed)
                    edits.bump()
                },
                mixed = option.isMixed(huds),
            )
        }

        is SharedChoice<*> -> SharedChoiceControl(option, huds, edits)
    }
}

@Composable
private fun <E : Any> SharedChoiceControl(option: SharedChoice<E>, huds: List<Hud>, edits: SharedEdits) {
    val value = option.resolve(huds)
    val shown = (value as? SharedValue.Single)?.value ?: option.read(huds.first())
    val onChange = { choice: E ->
        option.apply(huds, choice)
        edits.bump()
    }
    val icon = option.icon
    if (icon != null) {
        ChoiceRadio(option.label, option.values, shown, icon, onChange, value.isMixed())
    } else {
        ChoiceDropdown(option.label, option.values, shown, option.name, onChange, mixed = value.isMixed())
    }
}

/** Counts display changes of the options mods defined, since which of them every HUD shows depends on them */
@Composable
private fun rememberDisplayRevision(huds: List<Hud>): Int {
    var revision by remember(huds) { mutableIntStateOf(0) }
    DisposableEffect(huds) {
        val listener = Consumer<Property.Display> { revision++ }
        val props = huds.flatMap(SharedProperties::candidates)
        props.forEach { it.addDisplayListener(listener) }
        onDispose { props.forEach { it.removeDisplayListener(listener) } }
    }
    return revision
}

private fun SharedProperty.matches(query: String): Boolean {
    val prop = props.first()
    return listOfNotNull(prop.localizedTitle(), prop.id, prop.localizedDescription()).any { searchMatches(it.asRenderText(), query) }
}

/** The options mods defined, which are drawn the way the HUD editor draws the options of a single HUD */
@Composable
private fun SharedPropertyRows(huds: List<Hud>, query: String, edits: SharedEdits) {
    val revision = rememberDisplayRevision(huds)
    val shared = remember(huds, revision) { SharedProperties.find(huds) }
    if (shared.isEmpty()) return
    val title = "Options"
    Section(title, query, *shared.map { it.props.first().localizedTitle().asRenderText() }.toTypedArray()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (option in shared) {
                if (query.isEmpty() || searchMatches(title, query) || option.matches(query)) {
                    key(option.id) { SharedPropertyRow(option, edits) }
                }
            }
        }
    }
}

@Composable
private fun SharedPropertyRow(shared: SharedProperty, edits: SharedEdits) {
    val proxy = remember { shared.proxy(onWrite = edits::bump) }
    DisposableEffect(proxy) {
        val listener = Consumer<Property.Display> { proxy.revaluateDisplay() }
        shared.props.forEach { it.addDisplayListener(listener) }
        onDispose { shared.props.forEach { it.removeDisplayListener(listener) } }
    }
    if (rememberDisplay(proxy) == Property.Display.HIDDEN) return
    // read so the note reads the HUDs again after an edit
    edits.revision
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HudOptionRow(proxy)
        if (shared.resolve().isMixed()) {
            Text(
                "Mixed between these HUDs, changing it sets it for all of them.",
                color = LocalTheme.current.textColorSecondary,
                fontSize = 12.sp,
            )
            GroupRow("Set every HUD to the value shown", "align") { proxy.setReferential(proxy.get()) }
        }
    }
}

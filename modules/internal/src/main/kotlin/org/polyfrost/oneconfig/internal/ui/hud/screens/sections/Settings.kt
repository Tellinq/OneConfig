package org.polyfrost.oneconfig.internal.ui.hud.screens.sections

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudGroup
import org.polyfrost.oneconfig.api.hud.v1.HudGroups
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.HudResize
import org.polyfrost.oneconfig.api.hud.v1.LegacyHudMarker
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.blockInteraction
import org.polyfrost.oneconfig.internal.ui.components.localizedLabel
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.components.settings.NumberSpinner
import org.polyfrost.oneconfig.internal.ui.components.settings.SwitchControl
import org.polyfrost.oneconfig.internal.ui.hud.HudSettingTarget
import org.polyfrost.oneconfig.internal.ui.hud.HudSettingsContent
import org.polyfrost.oneconfig.internal.ui.hud.hudHasPositionDefaults
import org.polyfrost.oneconfig.internal.ui.hud.repairHudStaticSize
import org.polyfrost.oneconfig.internal.ui.hud.resetHudPosition
import org.polyfrost.oneconfig.internal.ui.hud.screens.hudGroupLabel
import org.polyfrost.oneconfig.internal.ui.screens.HudConfigScreen
import org.polyfrost.oneconfig.internal.ui.themes.AccentBrush
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

private val DeleteButtonColor = Color(0xFFE5484D)

/** [Hud.hudScale], read straight from the HUD so dragging a corner handle updates it while the panel is open */
@Composable
private fun HudScaleSpinner(hud: Hud, optionId: String) {
    val range = hud.hudScaleRange
    HudSettingTarget(hud, optionId) {
        NumberSpinner(
            hud.hudScale,
            { Snapshot.withMutableSnapshot { hud.hudScale = it } },
            range.start,
            range.endInclusive,
            0.25f,
            width = 176.dp,
            format = { "%.2f".format(it) },
            unit = "x",
            label = "Scale",
        )
    }
}

/** What the HUDs of [group] share, with [onOpen] stepping into the group to reach what is inside it */
@Composable
fun GroupSettings(group: HudGroup, query: String, onOpen: (Set<Hud>) -> Unit) {
    // the group reads the HUD list, which compose cannot see change
    HudManager.instancesRevision.intValue
    val theme = LocalTheme.current
    val locked = group.locked
    Section("Position", query, "Lock position") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SwitchControl(locked) { HudGroups.setLocked(group, it) }
                Text("Lock position", color = theme.textColor, fontSize = 14.sp)
            }
            if (!locked && group.huds.any { it.locked }) {
                Text("Some HUDs are locked on their own and hold still inside the group.", color = theme.textColorSecondary, fontSize = 12.sp)
            }
        }
    }
    val scalable = group.huds.any { it.resizeAxes == HudResize.Both }
    if (scalable) Section("Scale", query, "Match scale") {
        val range = HudGroups.scaleRange(group)
        val scale = group.scale
        Column(
            modifier = Modifier.blockInteraction(locked),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberSpinner(
                scale,
                { HudGroups.setScale(group, it) },
                range.start,
                range.endInclusive,
                0.25f,
                width = 176.dp,
                format = { "%.2f".format(it) },
                unit = "x",
                label = "Scale",
            )
            if (!group.uniformScale) {
                GroupRow("Set every HUD to %.2fx".format(scale), "align") { HudGroups.matchScale(group, scale) }
            }
        }
    }
    Section("HUDs", query) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            for (sub in group.subGroups) {
                GroupRow(hudGroupLabel(sub), "group") { onOpen(sub.huds.toSet()) }
            }
            for (hud in group.directHuds) {
                GroupRow(localizedLabel(hud.title) ?: hud.title, "right-arrow") { onOpen(setOf(hud)) }
            }
        }
    }
}

@Composable
private fun GroupRow(text: String, icon: String, onClick: () -> Unit) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val background by animateColorAsState(theme.textColor.copy(alpha = if (isHovered) 0.10f else 0f))
    Row(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .background(background, theme.buttonShape)
            .onClick(interactionSource, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, color = theme.textColorSecondary, modifier = Modifier.size(14.dp))
        Text(text, color = theme.textColor, fontSize = 13.sp)
    }
}

@Composable
fun Settings(hud: Hud?, query: String, onDeleted: () -> Unit) {
    if (hud == null) return

    LaunchedEffect(hud) { repairHudStaticSize(hud) }

    HudSettingsContent(hud) {
        var useGuiScale by remember { mutableStateOf(hud.useGuiScale) }
        var customScale by remember { mutableStateOf(hud.customScale) }
        var showBackground by remember { mutableStateOf(hud.showBackground) }
        var bgColor by remember { mutableStateOf(Color(hud.bgColor)) }
        var bgChroma by remember { mutableStateOf(hud.bgChroma) }
        var bgChromaSpeed by remember { mutableStateOf(hud.bgChromaSpeed) }
        var bgRadius by remember { mutableStateOf(hud.bgRadius) }
        var mergeBackground by remember { mutableStateOf(hud.mergeBackground) }
        var mergeDiagonally by remember { mutableStateOf(hud.mergeDiagonally) }
        var keepBgWhenHidden by remember { mutableStateOf(hud.keepBgWhenHidden) }
        var textColor by remember { mutableStateOf(Color(hud.textColor)) }
        var textChroma by remember { mutableStateOf(hud.textChroma) }
        var textChromaSpeed by remember { mutableStateOf(hud.textChromaSpeed) }
        var showShadow by remember { mutableStateOf(hud.showShadow) }
        var fullShadow by remember { mutableStateOf(hud.fullShadow) }
        var shadowColor by remember { mutableStateOf(Color(hud.shadowColor)) }
        var shadowChroma by remember { mutableStateOf(hud.shadowChroma) }
        var shadowChromaSpeed by remember { mutableStateOf(hud.shadowChromaSpeed) }
        var showInF3 by remember { mutableStateOf(hud.showInF3) }
        var showInTab by remember { mutableStateOf(hud.showInTab) }
        var showInScreens by remember { mutableStateOf(hud.showInScreens) }
        var showInChat by remember { mutableStateOf(hud.showInChat) }
        var locked by remember { mutableStateOf(hud.locked) }

        Section("Position", query, "Lock position", "Reset to default position") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ResetHudPositionButton(hud)
                HudSettingTarget(hud, "locked") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SwitchControl(locked) {
                            Snapshot.withMutableSnapshot { locked = it; hud.locked = it }
                        }
                        Text("Lock position", color = LocalTheme.current.textColor, fontSize = 14.sp)
                    }
                }
            }
        }

        if (hud is LegacyHudMarker) {
            if (hud.supportsScale) {
                Section("Scale", query) {
                    HudScaleSpinner(hud, "customScale")
                }
            }
        } else {
            if (hud.resizeAxes == HudResize.Both) {
                Section("Scale", query) {
                    HudScaleSpinner(hud, "textScale")
                }
            }
            Section("GUI Scale", query, "Use Minecraft GUI Scale", "Custom Scale") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HudSettingTarget(hud, "useGuiScale") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(useGuiScale) {
                                Snapshot.withMutableSnapshot { useGuiScale = it; hud.useGuiScale = it }
                            }
                            Text("Use Minecraft GUI Scale", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    HudSettingTarget(hud, "customScale") {
                        NumberSpinner(
                            customScale,
                            { Snapshot.withMutableSnapshot { customScale = it; hud.customScale = it } },
                            0.25f,
                            4f,
                            0.25f,
                            width = 176.dp,
                            unit = "x",
                            label = "Custom Scale",
                        )
                    }
                }
            }
        }

        if (hud !is LegacyHudMarker) {
            Section(
                "Background", query,
                "Show Background", "Background Color", "Radius",
                "Merge With Neighbours", "Merge Diagonally", "Keep Background When Hidden",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HudSettingTarget(hud, "showBackground") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showBackground) {
                                Snapshot.withMutableSnapshot { showBackground = it; hud.showBackground = it }
                            }
                            Text("Show Background", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    if (showBackground) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HudSettingTarget(hud, "bgColor") {
                                ColorButton(
                                    "Background Color", bgColor,
                                    chroma = bgChroma, chromaSpeed = bgChromaSpeed,
                                    onColorChanged = {
                                        Snapshot.withMutableSnapshot { bgColor = it; hud.bgColor = it.toArgb() }
                                    },
                                    onChromaChanged = { en, sp ->
                                        Snapshot.withMutableSnapshot {
                                            bgChroma = en; hud.bgChroma = en
                                            bgChromaSpeed = sp; hud.bgChromaSpeed = sp
                                        }
                                    },
                                )
                            }
                            HudSettingTarget(hud, "bgRadius") {
                                NumberSpinner(
                                    bgRadius, { Snapshot.withMutableSnapshot { bgRadius = it; hud.bgRadius = it } },
                                    0f, 32f, 1f, width = 116.dp, unit = "px", label = "Radius"
                                )
                            }
                        }
                        // HUDs drawing their own background opt out of merging so these switches
                        // have nothing to control
                        if (hud.canMergeBackground()) {
                            HudSettingTarget(hud, "mergeBackground") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    SwitchControl(mergeBackground) {
                                        Snapshot.withMutableSnapshot { mergeBackground = it; hud.mergeBackground = it }
                                    }
                                    Text("Merge With Neighbours", color = LocalTheme.current.textColor, fontSize = 14.sp)
                                }
                            }
                            if (mergeBackground) {
                                HudSettingTarget(hud, "mergeDiagonally") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        SwitchControl(mergeDiagonally) {
                                            Snapshot.withMutableSnapshot { mergeDiagonally = it; hud.mergeDiagonally = it }
                                        }
                                        Text("Merge Diagonally", color = LocalTheme.current.textColor, fontSize = 14.sp)
                                    }
                                }
                                HudSettingTarget(hud, "keepBgWhenHidden") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        SwitchControl(keepBgWhenHidden) {
                                            Snapshot.withMutableSnapshot { keepBgWhenHidden = it; hud.keepBgWhenHidden = it }
                                        }
                                        Text("Keep Background When Hidden", color = LocalTheme.current.textColor, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Section("Appearance", query, "Text Color", "Text Shadow", "Full Shadow", "Shadow Color") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HudSettingTarget(hud, "textColor") {
                        ColorButton(
                            "Text Color", textColor,
                            chroma = textChroma, chromaSpeed = textChromaSpeed,
                            onColorChanged = {
                                Snapshot.withMutableSnapshot { textColor = it; hud.textColor = it.toArgb() }
                            },
                            onChromaChanged = { en, sp ->
                                Snapshot.withMutableSnapshot {
                                    textChroma = en; hud.textChroma = en
                                    textChromaSpeed = sp; hud.textChromaSpeed = sp
                                }
                            },
                        )
                    }
                    HudSettingTarget(hud, "showShadow") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showShadow) {
                                Snapshot.withMutableSnapshot { showShadow = it; hud.showShadow = it }
                            }
                            Text("Text Shadow", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    if (showShadow) {
                        HudSettingTarget(hud, "fullShadow") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                SwitchControl(fullShadow) {
                                    Snapshot.withMutableSnapshot { fullShadow = it; hud.fullShadow = it }
                                }
                                Text("Full Shadow", color = LocalTheme.current.textColor, fontSize = 14.sp)
                            }
                        }
                        HudSettingTarget(hud, "shadowColor") {
                            ColorButton(
                                "Shadow Color", shadowColor,
                                chroma = shadowChroma, chromaSpeed = shadowChromaSpeed,
                                onColorChanged = {
                                    Snapshot.withMutableSnapshot { shadowColor = it; hud.shadowColor = it.toArgb() }
                                },
                                onChromaChanged = { en, sp ->
                                    Snapshot.withMutableSnapshot {
                                        shadowChroma = en; hud.shadowChroma = en
                                        shadowChromaSpeed = sp; hud.shadowChromaSpeed = sp
                                    }
                                },
                            )
                        }
                    }
                }
            }

            Section("Visibility", query, "Show in F3 Screen", "Show in Tab List", "Show in GUIs", "Show in Chat") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HudSettingTarget(hud, "showInF3") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showInF3) {
                                Snapshot.withMutableSnapshot { showInF3 = it; hud.showInF3 = it }
                            }
                            Text("Show in F3 Screen", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    HudSettingTarget(hud, "showInTab") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showInTab) {
                                Snapshot.withMutableSnapshot { showInTab = it; hud.showInTab = it }
                            }
                            Text("Show in Tab List", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    HudSettingTarget(hud, "showInScreens") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showInScreens) {
                                Snapshot.withMutableSnapshot { showInScreens = it; hud.showInScreens = it }
                            }
                            Text("Show in GUIs", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                    HudSettingTarget(hud, "showInChat") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SwitchControl(showInChat) {
                                Snapshot.withMutableSnapshot { showInChat = it; hud.showInChat = it }
                            }
                            Text("Show in Chat", color = LocalTheme.current.textColor, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        hud.tree?.let { HudConfigScreen(it, query) }

        if (hud.canDelete() && hudSearchMatches(query, "Delete HUD")) {
            DeleteHudButton(hud, onDeleted)
        }
    }
}

@Composable
private fun ResetHudPositionButton(hud: Hud) {
    val theme = LocalTheme.current
    val enabled = hudHasPositionDefaults(hud)
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val disabledAlpha by animateFloatAsState(if (enabled) 0f else 1f)
    val accentAlpha by animateFloatAsState(
        when {
            !enabled -> 0f
            isHovered -> 0.75f
            else -> 1f
        },
    )
    val textColor = if (enabled) theme.accentTextColor else theme.textColorSecondary

    Box(
        modifier = Modifier
            .then(if (enabled) Modifier.pointerHoverIcon(PointerIcon.Hand) else Modifier)
            .background(theme.borderColor.copy(alpha = theme.borderColor.alpha * disabledAlpha), theme.buttonShape)
            .background(AccentBrush, theme.buttonShape, alpha = accentAlpha)
            .then(
                if (enabled) {
                    Modifier.onClick(interactionSource) { resetHudPosition(hud) }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Reset to default position", color = textColor, fontSize = 13.sp)
    }
}

@Composable
private fun DeleteHudButton(hud: Hud, onDeleted: () -> Unit) {
    val theme = LocalTheme.current
    val interactionSource = rememberInteractionSource()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val bgColor by animateColorAsState(
        if (isHovered) DeleteButtonColor.copy(alpha = 0.75f) else DeleteButtonColor,
    )

    Box(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .background(bgColor, theme.buttonShape)
            .onClick(interactionSource) {
                Snapshot.withMutableSnapshot {
                    HudManager.removeHud(hud, delete = true)
                }
                onDeleted()
            }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon("trash", color = theme.accentTextColor, modifier = Modifier.size(14.dp))
            Text("Delete HUD", color = theme.accentTextColor, fontSize = 13.sp)
        }
    }
}

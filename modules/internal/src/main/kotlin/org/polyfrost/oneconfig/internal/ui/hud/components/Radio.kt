package org.polyfrost.oneconfig.internal.ui.hud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.internal.ui.components.SelectableIconButton
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.components.onClick
import org.polyfrost.oneconfig.internal.ui.components.rememberInteractionSource
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme

interface RadioValue {
    val icon: String
}

@Composable
inline fun <reified E> Radio(
    label: String,
    value: E,
    mixed: Boolean = false,
    crossinline onChange: (E) -> Unit,
) where E : RadioValue, E : Enum<E> {
    ChoiceRadio(label, enumValues<E>().toList(), value, { it.icon }, { onChange(it) }, mixed)
}

/**
 * @param mixed marks no choice as picked and says so beside [label], for a setting that differs between
 * several things
 */
@Composable
fun <T> ChoiceRadio(
    label: String,
    values: List<T>,
    value: T,
    icon: (T) -> String,
    onChange: (T) -> Unit,
    mixed: Boolean = false,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(label, color = LocalTheme.current.textColor, fontSize = 14.sp)
            if (mixed) Text("Mixed", color = LocalTheme.current.textColorSecondary, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier.background(Color(0x192126).copy(.7f), RoundedCornerShape(6.dp))
                .border(1.dp, LocalTheme.current.borderColor, RoundedCornerShape(6.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                values.forEach {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .onClick(rememberInteractionSource()) {
                                Snapshot.withMutableSnapshot {
                                    onChange(it)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        SelectableIconButton(
                            icon(it),
                            selected = !mixed && value == it,
                            modifier = Modifier.size(18.dp),
                            onClick = {
                                Snapshot.withMutableSnapshot {
                                    onChange(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.split_decrease
import com.eyther.lumbridge.features.expenses.generated.resources.split_increase
import com.eyther.lumbridge.features.expenses.generated.resources.split_value
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import org.jetbrains.compose.resources.stringResource

private val StepperTargetSize = 48.dp // Preserve full touch targets beside direct numeric input.
private val StepperIconSize = 18.dp // A quiet icon inside the full-size target.
private val StepperStrokeWidth = 2.dp // Match the weight of Loom's outlined icons.
private val ShareInputWidth = 64.dp // One share value fits beside two full-size touch targets.
private val AmountInputWidth = 96.dp // Decimal/percentage input needs more room than a share count.

@Composable
internal fun ExpenseSplitParticipantRow(
    member: MemberDomain,
    method: ExpenseSplitMethodDomain,
    selected: Boolean,
    value: String,
    amount: String?,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onToggle: () -> Unit,
    onValue: (String) -> Unit,
    onStep: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            modifier = Modifier.semantics { contentDescription = member.name },
        )
        MemberAvatar(
            memberId = member.id,
            initials = member.name.trim().take(1).uppercase(),
            picturePath = member.avatarPath,
        )
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(member.name, style = MaterialTheme.typography.titleSmall)
            amount?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (selected && method != ExpenseSplitMethodDomain.EQUAL) {
            SplitStepperButton(
                increase = false,
                label = stringResource(Res.string.split_decrease, member.name),
                enabled = canDecrease,
                modifier = Modifier.testTag("split_decrease_${member.id}"),
                onClick = { onStep(false) },
            )
            val participantLabel = stringResource(Res.string.split_value, member.name)
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            if (method == ExpenseSplitMethodDomain.SHARES) KeyboardType.Number
                            else KeyboardType.Decimal,
                    ),
                modifier =
                    Modifier.width(
                            if (method == ExpenseSplitMethodDomain.SHARES) ShareInputWidth
                            else AmountInputWidth,
                        )
                        .testTag("split_${member.id}")
                        .semantics { contentDescription = participantLabel },
            )
            SplitStepperButton(
                increase = true,
                label = stringResource(Res.string.split_increase, member.name),
                enabled = canIncrease,
                modifier = Modifier.testTag("split_increase_${member.id}"),
                onClick = { onStep(true) },
            )
        }
    }
}

@Composable
private fun SplitStepperButton(
    increase: Boolean,
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.size(StepperTargetSize).semantics { contentDescription = label },
    ) {
        val color = LocalContentColor.current
        Canvas(Modifier.size(StepperIconSize)) {
            drawLine(
                color,
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = StepperStrokeWidth.toPx(),
                cap = StrokeCap.Round,
            )
            if (increase)
                drawLine(
                    color,
                    Offset(size.width / 2, 0f),
                    Offset(size.width / 2, size.height),
                    strokeWidth = StepperStrokeWidth.toPx(),
                    cap = StrokeCap.Round,
                )
        }
    }
}

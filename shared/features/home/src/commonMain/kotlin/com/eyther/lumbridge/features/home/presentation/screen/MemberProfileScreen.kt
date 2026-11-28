package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.presentation.components.PayWithSection
import com.eyther.lumbridge.features.expenses.presentation.model.PayOptionsUi
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_former_member
import com.eyther.lumbridge.features.home.generated.resources.member_profile_balance
import com.eyther.lumbridge.features.home.generated.resources.member_profile_gone
import com.eyther.lumbridge.features.home.generated.resources.member_profile_no_payment
import com.eyther.lumbridge.features.home.generated.resources.member_profile_no_tasks
import com.eyther.lumbridge.features.home.generated.resources.member_profile_owes
import com.eyther.lumbridge.features.home.generated.resources.member_profile_owes_you
import com.eyther.lumbridge.features.home.generated.resources.member_profile_pay
import com.eyther.lumbridge.features.home.generated.resources.member_profile_payment
import com.eyther.lumbridge.features.home.generated.resources.member_profile_settled
import com.eyther.lumbridge.features.home.generated.resources.member_profile_tasks
import com.eyther.lumbridge.features.home.generated.resources.member_profile_you
import com.eyther.lumbridge.features.home.generated.resources.member_profile_you_owe
import com.eyther.lumbridge.features.home.generated.resources.ui_try_again
import com.eyther.lumbridge.features.home.presentation.components.HomeTaskRow
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.home.presentation.model.MemberSettlementUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.MemberProfileScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.components.MemberRoleChip
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

private val ProfileAvatarSize = 112.dp

/**
 * A housemate's profile from household details: their picture, how to pay them, where they stand
 * on the everyday account and what is assigned to them. [onTasks] opens Tasks.
 */
@Composable
fun MemberProfileScreen(viewModel: MemberProfileScreenViewModelContract, onTasks: () -> Unit) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val profile = state.profile
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerMemberProfileScreen(
            memberId = profile?.member?.id.orEmpty(),
            name = profile?.member?.name.orEmpty(),
            avatarPath = profile?.member?.avatarPath,
            isOwner = profile?.member?.isOwner == true,
            isCurrentUser = state.isCurrentUser,
            loading = state.loading,
            missing = state.missing,
            error = state.loadError,
            settlements = state.settlements,
            payOptions = state.payOptions,
            tasks = state.tasks,
            onRetry = viewModel::onRetry,
            onTasks = onTasks,
        )
    }
}

@Composable
private fun InnerMemberProfileScreen(
    memberId: String,
    name: String,
    avatarPath: String?,
    isOwner: Boolean,
    isCurrentUser: Boolean,
    loading: Boolean,
    missing: Boolean,
    error: TextResource?,
    settlements: ImmutableList<MemberSettlementUi>,
    payOptions: PayOptionsUi?,
    tasks: ImmutableList<HomeTaskUi>,
    onRetry: () -> Unit,
    onTasks: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("member_profile"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        error?.let { message ->
            item(key = "error") {
                RetryNotice(message = message, onRetry = onRetry, retryText = stringResource(Res.string.ui_try_again))
            }
        }
        when {
            loading -> item(key = "loading") { LoadingRows(rows = 4) }
            missing ->
                item(key = "missing") {
                    Text(
                        stringResource(Res.string.member_profile_gone),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            memberId.isNotEmpty() -> {
                item(key = "header") {
                    ProfileHeader(
                        memberId = memberId,
                        name = name,
                        avatarPath = avatarPath,
                        isOwner = isOwner,
                        isCurrentUser = isCurrentUser,
                    )
                }
                item(key = "balance") { BalanceSection(settlements = settlements) }
                if (!isCurrentUser)
                    item(key = "payment") {
                        PaymentSection(name = name, payOptions = payOptions)
                    }
                item(key = "tasks") { TasksSection(tasks = tasks, onTasks = onTasks) }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    memberId: String,
    name: String,
    avatarPath: String?,
    isOwner: Boolean,
    isCurrentUser: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatar(
            memberId = memberId,
            name = name,
            picturePath = avatarPath,
            size = ProfileAvatarSize,
            modifier = Modifier.testTag("member_profile_picture"),
        )
        Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MemberRoleChip(isOwner = isOwner)
            if (isCurrentUser)
                Text(
                    stringResource(Res.string.member_profile_you),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

@Composable
private fun BalanceSection(settlements: ImmutableList<MemberSettlementUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        SectionTitle(title = stringResource(Res.string.member_profile_balance))
        GroupedSurface(modifier = Modifier.testTag("member_balance")) {
            if (settlements.isEmpty())
                Text(
                    stringResource(Res.string.member_profile_settled),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(LoomSpacing.content),
                )
            settlements.forEachIndexed { index, settlement ->
                if (index > 0) LoomDivider()
                SettlementRow(settlement = settlement)
            }
        }
    }
}

@Composable
private fun SettlementRow(settlement: MemberSettlementUi) {
    val former = stringResource(Res.string.home_former_member)
    val text =
        when {
            settlement.fromIsYou ->
                stringResource(Res.string.member_profile_you_owe, settlement.toName ?: former)
            settlement.toIsYou ->
                stringResource(Res.string.member_profile_owes_you, settlement.fromName ?: former)
            else ->
                stringResource(
                    Res.string.member_profile_owes,
                    settlement.fromName ?: former,
                    settlement.toName ?: former,
                )
        }
    Row(
        modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        MemberAvatar(
            memberId = settlement.otherId,
            name = settlement.otherName,
            picturePath = settlement.otherAvatarPath,
        )
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            settlement.amount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * How to pay them. When you owe them PayPal opens with that amount filled in ("Pay Alex"); otherwise
 * the section lists their details.
 */
@Composable
private fun PaymentSection(name: String, payOptions: PayOptionsUi?) {
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        SectionTitle(title = stringResource(Res.string.member_profile_payment))
        if (payOptions != null)
            PayWithSection(
                options = payOptions,
                title =
                    if (payOptions.amount != null)
                        stringResource(Res.string.member_profile_pay, name, payOptions.amountLabel)
                    else null,
                hint = null,
                modifier = Modifier.testTag("member_payment"),
            )
        else
            Text(
                stringResource(Res.string.member_profile_no_payment, name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

@Composable
private fun TasksSection(tasks: ImmutableList<HomeTaskUi>, onTasks: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        SectionTitle(title = stringResource(Res.string.member_profile_tasks))
        if (tasks.isEmpty())
            Text(
                stringResource(Res.string.member_profile_no_tasks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        else
            GroupedSurface(modifier = Modifier.testTag("member_tasks")) {
                tasks.forEachIndexed { index, task ->
                    if (index > 0) LoomDivider()
                    HomeTaskRow(task = task, onClick = onTasks)
                }
            }
    }
}

package com.eyther.lumbridge.features.account.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.sign_out
import com.eyther.lumbridge.features.account.generated.resources.sign_out_message
import com.eyther.lumbridge.features.account.generated.resources.sign_out_title
import com.eyther.lumbridge.features.account.generated.resources.ui_cancel
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain

data class AccountState(
    val profile: ProfileDomain? = null,
    val nickname: String = "",
    val showNickname: Boolean = false,
    val showSignOutConfirmation: Boolean = false,
    val nicknameError: TextResource? = null,
    val preferences: SettingsDomain = SettingsDomain(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {
    val signOutConfirmation: DestructiveConfirmationUi? =
        if (showSignOutConfirmation) {
            DestructiveConfirmationUi(
                title = TextResource.Resource(Res.string.sign_out_title),
                message = TextResource.Resource(Res.string.sign_out_message),
                confirmText = TextResource.Resource(Res.string.sign_out),
                cancelText = TextResource.Resource(Res.string.ui_cancel),
            )
        } else null
    val theme: ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == preferences.theme } ?: ThemeMode.SYSTEM
    val displayError: TextResource? = error ?: loadError
    val canEditNickname: Boolean = profile != null && !busy
    val canSaveNickname: Boolean =
        nickname.trim().isNotEmpty() && nickname.trim() != profile?.nickname && !busy
}

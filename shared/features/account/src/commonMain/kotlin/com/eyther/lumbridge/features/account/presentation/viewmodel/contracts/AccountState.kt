package com.eyther.lumbridge.features.account.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
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
    /** What this person shares for being paid back, and the sheet's draft of it. */
    val paymentHandles: PaymentHandlesDomain? = null,
    val showPaymentDetails: Boolean = false,
    val mbwayPhone: String = "",
    val revolutUsername: String = "",
    val paypalUsername: String = "",
    val paymentError: TextResource? = null,
    val showSignOutConfirmation: Boolean = false,
    val nicknameError: TextResource? = null,
    val preferences: SettingsDomain = SettingsDomain(),
    val language: AppLanguage = AppLanguage.SYSTEM,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val savingPicture: Boolean = false,
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
    /** How many ways to be paid are shared, for the profile row. */
    val paymentMethodCount: Int =
        listOfNotNull(
                paymentHandles?.mbwayPhone,
                paymentHandles?.revolutUsername,
                paymentHandles?.paypalUsername,
            )
            .size

    val canEditNickname: Boolean = profile != null && !busy
    val hasPicture: Boolean = profile?.avatarPath != null
    val canChangePicture: Boolean = profile != null && !busy && !savingPicture
    val canSaveNickname: Boolean =
        nickname.trim().isNotEmpty() && nickname.trim() != profile?.nickname && !busy
}

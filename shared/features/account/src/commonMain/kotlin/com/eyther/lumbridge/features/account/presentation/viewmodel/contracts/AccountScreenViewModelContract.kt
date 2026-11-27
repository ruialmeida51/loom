package com.eyther.lumbridge.features.account.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.errors.PictureError
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class AccountScreenViewModelContract : LoomViewModel() {
    data class Params(val userId: String)

    abstract val state: StateFlow<AccountState>

    abstract fun onSignOutRequested()

    abstract fun onSignOutCancelled()

    abstract fun onEditNickname()

    abstract fun onNicknameChanged(value: String)

    abstract fun onSaveNickname()

    abstract fun onCloseNickname()

    abstract fun onEditPaymentDetails()

    abstract fun onMbwayPhoneChanged(value: String)

    abstract fun onRevolutUsernameChanged(value: String)

    abstract fun onPaypalUsernameChanged(value: String)

    abstract fun onSavePaymentDetails()

    abstract fun onClosePaymentDetails()

    abstract fun onThemeSelected(value: String)

    abstract fun onLanguageSelected(language: AppLanguage)

    abstract fun onNotificationsChanged(enabled: Boolean)

    /** A photo picked from the library becomes the profile picture. */
    abstract fun onPicturePicked(bytes: ByteArray)

    abstract fun onRemovePicture()

    /** The picker could not read the chosen photo. */
    abstract fun onPictureError(error: PictureError)

    abstract fun onRetry()

    abstract fun onDismissError()
}

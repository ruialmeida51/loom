package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.authentication.generated.resources.*
import com.eyther.lumbridge.features.authentication.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationScreen(vm: AuthenticationScreenViewModelContract) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerAuthenticationScreen(
        email = state.email,
        password = state.password,
        registering = state.registering,
        busy = state.busy,
        message = localizedMessage(state.message),
        error = state.error,
        onEmail = vm::email,
        onPassword = vm::password,
        onToggleRegister = vm::toggleRegister,
        onSubmit = vm::submit,
        onDismiss = vm::dismiss,
    )
}

@Composable
private fun InnerAuthenticationScreen(
    email: String,
    password: String,
    registering: Boolean,
    busy: Boolean,
    message: String?,
    error: String?,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onToggleRegister: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        PageHeading(
            stringResource(Res.string.ui_welcome_to_loom),
            stringResource(Res.string.ui_life_woven_together),
            stringResource(Res.string.ui_a_calmer_home_for_the_things_you_share),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (registering) stringResource(Res.string.ui_make_yourself_at_home)
            else stringResource(Res.string.ui_welcome_home),
            style = MaterialTheme.typography.headlineMedium,
        )
        ErrorNotice(localizedMessage(error), onDismiss)
        message?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        OutlinedTextField(
            email,
            onEmail,
            label = { Text(stringResource(Res.string.ui_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            password,
            onPassword,
            label = { Text(stringResource(Res.string.ui_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        LoomButton(
            if (registering) stringResource(Res.string.ui_create_account)
            else stringResource(Res.string.ui_sign_in),
            onSubmit,
            Modifier.fillMaxWidth(),
            loading = busy,
        )
        TextButton(onClick = onToggleRegister) {
            Text(
                if (registering) stringResource(Res.string.ui_already_have_an_account_sign_in)
                else stringResource(Res.string.ui_new_here_create_an_account),
            )
        }
    }
}

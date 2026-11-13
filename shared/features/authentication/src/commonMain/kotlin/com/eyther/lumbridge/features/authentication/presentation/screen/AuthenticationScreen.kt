package com.eyther.lumbridge.features.authentication.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import kotlinx.coroutines.flow.*

@Composable
fun AuthenticationScreen(vm: AuthenticationScreenViewModelContract) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerAuthenticationScreen(
        email = state.email,
        password = state.password,
        registering = state.registering,
        busy = state.busy,
        message = state.message,
        error = state.error,
        onEmail = vm::email,
        onPassword = vm::password,
        onToggleRegister = vm::toggleRegister,
        onSubmit = vm::submit,
        onDemo = vm::demo,
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
    onDemo: () -> Unit,
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
            "WELCOME TO LOOM",
            "Life, woven together.",
            "A calmer home for the things you share.",
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (registering) "Make yourself at home" else "Welcome home",
            style = MaterialTheme.typography.headlineMedium,
        )
        ErrorNotice(error, onDismiss)
        message?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        OutlinedTextField(
            email,
            onEmail,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            password,
            onPassword,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        LoomButton(
            if (registering) "Create account" else "Sign in",
            onSubmit,
            Modifier.fillMaxWidth(),
            loading = busy,
        )
        TextButton(onClick = onToggleRegister) {
            Text(
                if (registering) "Already have an account? Sign in"
                else "New here? Create an account",
            )
        }
        HorizontalDivider()
        OutlinedButton(onClick = onDemo, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Explore a sample household")
        }
    }
}

package dev.achmad.checkin.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import dev.achmad.checkin.R
import dev.achmad.checkin.core.di.util.injectLazy
import dev.achmad.checkin.domain.model.Company
import dev.achmad.checkin.domain.model.SignInOption
import dev.achmad.checkin.domain.preference.AuthPreference
import dev.achmad.checkin.presentation.screens.checkin.CheckInScreen
import dev.achmad.checkin.presentation.theme.CheckInTheme

object SignInScreen : Screen {
    private fun readResolve(): Any = SignInScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authPreference by remember { injectLazy<AuthPreference>() }
        val screenModel = rememberScreenModel { SignInScreenModel() }
        val state by screenModel.state.collectAsState()
        var shouldValidateChangeCompanyCode by remember { mutableStateOf(true) }
        var shouldNavigateToCompanyCode by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            screenModel.fetchCompany()
        }

        LaunchedEffect(shouldNavigateToCompanyCode) {
            if (shouldNavigateToCompanyCode) {
                navigator.replace(CompanyCodeScreen(shouldValidateChangeCompanyCode))
            }
        }

        LaunchedEffect(state.company) {
            if (state.company == null) return@LaunchedEffect
            val token = authPreference.token().get()
            if (token.isNotEmpty()) {
                navigator.replace(CheckInScreen)
            }
        }

        LaunchedEffect(state.shouldNavigateNext) {
            if (state.shouldNavigateNext) {
                navigator.replace(CheckInScreen)
            }
        }

        SignInScreen(
            state = state,
            onSignIn = { option ->
                screenModel.signIn(option)
            },
            onChangeCompany = {
                shouldValidateChangeCompanyCode = false
                shouldNavigateToCompanyCode = true
            },
            onRetryFetchCompany = {
                screenModel.fetchCompany()
            }
        )

    }

}

@Composable
private fun SignInScreen(
    state: SignInScreenState = SignInScreenState(),
    onSignIn: (SignInOption) -> Unit = {},
    onChangeCompany: () -> Unit = {},
    onRetryFetchCompany: () -> Unit = {},
) {
    var iconError by rememberSaveable { mutableStateOf(false) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (state.fetchCompanyLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.company == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something wrong happened",
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onRetryFetchCompany,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
        return
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Switch Company")
            },
            text = {
                Text("Switch to another company?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onChangeCompany()
                    },
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 36.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .then(if (iconError) Modifier.padding(8.dp) else Modifier),
        ) {
            AsyncImage(
                modifier = Modifier.size(if (iconError) 72.dp else 80.dp),
                model = state.company.iconUrl,
                contentDescription = null,
                error = painterResource(R.drawable.attendance_app_logo),
                onError = { iconError = true }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = state.company.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sign_in_description),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(36.dp))
        state.company.signInOptions.forEach { option ->
            when(option) {
                SignInOption.Name.BASIC -> {
                    var usernameTextFieldValue by rememberSaveable { mutableStateOf("") }
                    var passwordTextFieldValue by rememberSaveable { mutableStateOf("") }
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }
                    val continueEnabled by remember { 
                        derivedStateOf { 
                            usernameTextFieldValue.isNotEmpty() &&
                                    passwordTextFieldValue.isNotEmpty()
                        }
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.sign_in_basic_username_text_field_label),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = usernameTextFieldValue,
                            enabled = !state.signInLoading,
                            onValueChange = {
                                usernameTextFieldValue = it
                            },
                            placeholder = {
                                Text(stringResource(R.string.sign_in_basic_username_text_field_placeholder))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.sign_in_basic_password_text_field_label),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = passwordTextFieldValue,
                            enabled = !state.signInLoading,
                            visualTransformation = when {
                                passwordVisible -> VisualTransformation.None
                                else -> PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible }
                                ) {
                                    Icon(imageVector  = icon, null)
                                }
                            },
                            onValueChange = {
                                passwordTextFieldValue = it
                            },
                            placeholder = {
                                Text(stringResource(R.string.sign_in_basic_password_text_field_placeholder))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        onClick = {
                            onSignIn(
                                SignInOption.Basic(usernameTextFieldValue, passwordTextFieldValue)
                            )
                        },
                        enabled = continueEnabled,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (state.signInLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.sign_in_continue),
                            )
                        }
                    }
                }
                SignInOption.Name.UNKNOWN -> {}
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        shape = RoundedCornerShape(8.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sign_in_need_help_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.sign_in_need_help_description),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = { showDialog = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.sign_in_change_company))
        }
    }
}

@Preview
@Composable
private fun SignInScreenPreview() {
    CheckInTheme {
        SignInScreen(
            state = SignInScreenState(
                fetchCompanyLoading = false,
                company = Company(
                    id = "1",
                    name = "Kereta Cepat Indonesia Cina",
                    code = "KCIC",
                    iconUrl = "https://gbf.wiki/images/thumb/0/0b/Wilnas_%28Rising%29_square.png/110px-Wilnas_%28Rising%29_square.png",
                    signInOptions = listOf(SignInOption.Name.BASIC),
                    active = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
    }
}

@Preview
@Composable
private fun SignInScreenPreviewFetchCompanyError() {
    CheckInTheme {
        SignInScreen(
            state = SignInScreenState(
                fetchCompanyLoading = false
            )
        )
    }
}

@Preview
@Composable
private fun SignInScreenPreviewFetchCompanyLoading() {
    CheckInTheme {
        SignInScreen()
    }
}
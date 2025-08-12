package dev.achmad.rollcall.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.achmad.rollcall.R
import dev.achmad.rollcall.ui.theme.RollCallTheme

object CompanyCodeScreen : Screen {
    private fun readResolve(): Any = CompanyCodeScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CompanyCodeScreenModel() }
        val companyCode by screenModel.companyCode.collectAsState()
        val loading by screenModel.loading.collectAsState()
        val shouldNavigate by screenModel.shouldNavigate.collectAsState()

        var companyCodeTextFieldValue by remember { mutableStateOf(companyCode) }
        val continueEnabled by remember {
            derivedStateOf {
                companyCodeTextFieldValue.isNotEmpty()
            }
        }

        LaunchedEffect(Unit) {
            if (companyCode.isNotEmpty()) {
                screenModel.validateCompanyCode(companyCodeTextFieldValue)
            }
        }

        LaunchedEffect(shouldNavigate) {
            if (shouldNavigate) {
                navigator.replace(SignInScreen)
            }
        }

        CompanyCodeScreen(
            loading = loading,
            continueEnabled = continueEnabled,
            companyCode = companyCodeTextFieldValue,
            onCompanyCodeValueChanged = { companyCodeTextFieldValue = it },
            onContinue = { screenModel.validateCompanyCode(companyCodeTextFieldValue) }
        )

    }

}

@Composable
private fun CompanyCodeScreen(
    loading: Boolean = false,
    continueEnabled: Boolean = false,
    companyCode: String = "",
    onCompanyCodeValueChanged: (String) -> Unit = {},
    onContinue: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
        ) {
            Image(
                modifier = Modifier.size(48.dp),
                painter = painterResource(R.drawable.attendance_app_logo),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.company_code_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.company_code_description),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Column {
            Text(
                text = stringResource(R.string.company_code_text_field_label),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = companyCode,
                enabled = !loading,
                onValueChange = { onCompanyCodeValueChanged(it) },
                placeholder = {
                    Text(stringResource(R.string.company_code_text_field_placeholder))
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            onClick = onContinue,
            enabled = continueEnabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.surface
                )
            } else {
                Text(
                    text = stringResource(R.string.company_code_continue),
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
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
                        text = stringResource(R.string.company_code_need_help_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.company_code_need_help_description),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CompanyCodeScreenPreview() {
    RollCallTheme {
        CompanyCodeScreen()
    }
}

@Preview
@Composable
private fun CompanyCodeScreenPreviewButtonEnabled() {
    RollCallTheme {
        CompanyCodeScreen(
            companyCode = "ABC123"
        )
    }
}
package com.example.tracksy.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tracksy.ui.theme.TracksyAuthTypography
import com.example.tracksy.ui.theme.TracksySecondaryText
import com.example.tracksy.ui.theme.TracksyTheme

@Preview(name = "Auth Components", showBackground = true)
@Composable
private fun AuthComponentsPreview() {
    TracksyTheme {
        AuthScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthHeader(title = "Iniciar sesión")
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Correo electrónico"
                )
                AuthPasswordField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Contraseña"
                )
                AuthPrimaryButton(
                    text = "Ingresar",
                    onClick = {}
                )
                AuthSecondaryButton(
                    text = "Reenviar instrucciones",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                AuthLinkText(
                    text = "¿Olvidaste tu contraseña?",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                AuthInlineLinkText(
                    text = "¿No tenés cuenta?",
                    linkText = "Creá una",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                AuthSuccessMessage(
                    title = "Revisá tu correo",
                    body = "Si existe una cuenta asociada a ese correo, te enviaremos instrucciones para restablecer tu contraseña.",
                    helperText = "¿No lo recibiste? Revisá tu carpeta de spam."
                )
            }
        }
    }
}

@Preview(name = "Sign In Screen", showBackground = true)
@Composable
private fun SignInScreenPreview() {
    TracksyTheme {
        AuthScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.Top
            ) {
                AuthHeader(title = "Iniciar sesión")
                Spacer(modifier = Modifier.height(60.dp))
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(20.dp))
                AuthPasswordField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Contraseña"
                )
                Spacer(modifier = Modifier.height(38.dp))
                AuthPrimaryButton(
                    text = "Ingresar",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthLinkText(
                    text = "¿Olvidaste tu contraseña?",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthInlineLinkText(
                    text = "¿No tenés cuenta?",
                    linkText = "Creá una",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Create Account Screen", showBackground = true)
@Composable
private fun CreateAccountScreenPreview() {
    TracksyTheme {
        AuthScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.Top
            ) {
                AuthHeader(title = "Crear cuenta")
                Spacer(modifier = Modifier.height(60.dp))
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Nombre"
                )
                Spacer(modifier = Modifier.height(18.dp))
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(18.dp))
                AuthPasswordField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Contraseña"
                )
                Spacer(modifier = Modifier.height(18.dp))
                AuthPasswordField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Confirmá tu contraseña"
                )
                Spacer(modifier = Modifier.height(48.dp))
                AuthPrimaryButton(
                    text = "Crear cuenta",
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthInlineLinkText(
                    text = "¿Ya tenés cuenta?",
                    linkText = "Iniciá sesión",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Recover Password Screen", showBackground = true)
@Composable
private fun RecoverPasswordScreenPreview() {
    TracksyTheme {
        AuthScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.Top
            ) {
                AuthHeader(title = "Recuperar contraseña")
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Ingresá tu correo electrónico y te enviaremos instrucciones para restablecer tu contraseña.",
                    color = TracksySecondaryText,
                    style = TracksyAuthTypography.Body
                )
                Spacer(modifier = Modifier.height(30.dp))
                AuthTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Correo electrónico"
                )
                Spacer(modifier = Modifier.height(48.dp))
                AuthPrimaryButton(
                    text = "Enviar instrucciones",
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Check Email Screen", showBackground = true)
@Composable
private fun CheckEmailScreenPreview() {
    TracksyTheme {
        AuthScreenContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.Top
            ) {
                AuthHeader(title = "Revisá tu correo")
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Si existe una cuenta asociada a ese correo, te enviaremos instrucciones para restablecer tu contraseña.",
                    color = TracksySecondaryText,
                    style = TracksyAuthTypography.Body
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "¿No lo recibiste? Revisá tu carpeta de spam.",
                    color = TracksySecondaryText,
                    style = TracksyAuthTypography.Helper
                )
                Spacer(modifier = Modifier.height(28.dp))
                AuthPrimaryButton(
                    text = "Volver a iniciar sesión",
                    onClick = {},
                    dark = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthLinkText(
                    text = "Reenviar instrucciones",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

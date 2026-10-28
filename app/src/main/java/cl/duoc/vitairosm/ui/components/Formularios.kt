package cl.duoc.vitairosm.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.vitairosm.model.Campo
import cl.duoc.vitairosm.model.Catalogos

@Composable
fun CampoValidado(
    campo: Campo,
    etiqueta: String,
    icono: ImageVector,
    onCambio: (String) -> Unit,
    onPerdioFoco: () -> Unit,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false,
    ayuda: String? = null,
    maxCaracteres: Int? = null,
    lineas: Int = 1,
    imeAction: ImeAction = ImeAction.Next
) {
    var visible by remember { mutableStateOf(false) }
    var tuvoFoco by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = campo.valor,
        onValueChange = { nuevo -> onCambio(if (maxCaracteres != null) nuevo.take(maxCaracteres) else nuevo) },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { estado ->
                if (tuvoFoco && !estado.isFocused) onPerdioFoco()
                tuvoFoco = estado.isFocused
            },
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (campo.error != null) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (campo.valido) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Campo válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (esClave) {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "Ocultar" else "Ver")
                    }
                }
            }
        },
        isError = campo.error != null,
        visualTransformation = if (esClave && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = imeAction),
        singleLine = lineas == 1,
        minLines = lineas,
        maxLines = if (lineas == 1) 1 else lineas + 2,
        supportingText = {
            Column {
                AnimatedVisibility(visible = campo.error != null) {
                    Text(campo.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                if (campo.error == null && ayuda != null) {
                    Text(ayuda)
                }
                if (maxCaracteres != null) {
                    Text(
                        "${campo.valor.length}/$maxCaracteres",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    )
}

@Composable
fun SelectorAnimo(
    seleccionado: Int?,
    error: String?,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("¿Cómo te sientes ahora?", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Catalogos.ANIMOS.forEach { (valor, etiqueta) ->
                OpcionAnimo(valor, etiqueta, seleccionado == valor) { onSeleccion(valor) }
            }
        }
        AnimatedVisibility(visible = error != null) {
            Text(
                error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RowScope.OpcionAnimo(valor: Int, etiqueta: String, seleccionado: Boolean, onClick: () -> Unit) {
    val escala by animateFloatAsState(
        targetValue = if (seleccionado) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaAnimo"
    )
    val fondo by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "fondoAnimo"
    )
    val texto by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "textoAnimo"
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(escala)
                .background(fondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(valor.toString(), color = texto, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

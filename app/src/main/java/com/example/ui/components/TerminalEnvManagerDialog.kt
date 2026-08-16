package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.OpenCodeAmber
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeRed
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant

data class EnvVariable(
    val key: String,
    val value: String
)

@Composable
fun TerminalEnvManagerDialog(
    initialEnvVars: List<Pair<String, String>>,
    onSaveEnvVars: (List<Pair<String, String>>) -> Unit,
    onDismiss: () -> Unit
) {
    val envList = remember {
        mutableStateListOf<EnvVariable>().apply {
            if (initialEnvVars.isEmpty()) {
                add(EnvVariable("NODE_ENV", "development"))
                add(EnvVariable("PORT", "3000"))
                add(EnvVariable("LOG_LEVEL", "debug"))
                add(EnvVariable("API_BASE_URL", "http://localhost:8080"))
            } else {
                addAll(initialEnvVars.map { EnvVariable(it.first, it.second) })
            }
        }
    }

    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = OpenCodeAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Environment Variables (.env)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Text(
                    text = "Configure environment variables injected into sandbox execution contexts:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Add new variable inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it.uppercase().replace(" ", "_") },
                        placeholder = { Text("KEY", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.45f),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenCodeAmber,
                            unfocusedBorderColor = OpenCodeBorder
                        )
                    )

                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        placeholder = { Text("VALUE", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.45f),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OpenCodeAmber,
                            unfocusedBorderColor = OpenCodeBorder
                        )
                    )

                    IconButton(
                        onClick = {
                            if (newKey.isNotBlank()) {
                                envList.add(EnvVariable(newKey.trim(), newValue.trim()))
                                newKey = ""
                                newValue = ""
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(OpenCodeAmber)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Variables List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(envList) { index, item ->
                        Surface(
                            color = OpenCodeSurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.key,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = OpenCodeAmber
                                    )
                                    Text(
                                        text = " = ",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.value,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = OpenCodeMint
                                    )
                                }

                                IconButton(
                                    onClick = { envList.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = OpenCodeRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveEnvVars(envList.map { Pair(it.key, it.value) })
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OpenCodeAmber),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Save Variables", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

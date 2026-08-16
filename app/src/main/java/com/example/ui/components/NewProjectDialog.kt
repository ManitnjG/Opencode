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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OpenCodeBorder
import com.example.ui.theme.OpenCodeCyan
import com.example.ui.theme.OpenCodeMint
import com.example.ui.theme.OpenCodePurple
import com.example.ui.theme.OpenCodeSurface
import com.example.ui.theme.OpenCodeSurfaceElevated
import com.example.ui.theme.OpenCodeSurfaceVariant

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, description: String, templateType: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("nextjs") }

    val templates = listOf(
        Triple("nextjs", "Next.js 15 & React", "TypeScript, Tailwind CSS, API route handlers"),
        Triple("fastapi", "Python FastAPI Backend", "Pydantic v2, Pytest suite, async endpoints"),
        Triple("go", "Go Cloud Microservice", "Gin 1.23 REST API, goroutines, JSON binding"),
        Triple("flutter", "Flutter & Dart App", "Material 3 mobile UI, widget test suite"),
        Triple("reactnative", "React Native & Expo", "Universal iOS/Android, TypeScript, Expo Router"),
        Triple("springboot", "Spring Boot 3.4 Kotlin", "Enterprise REST API, Gradle Kotlin DSL"),
        Triple("compose", "Android Jetpack Compose", "Kotlin, Material 3, Clean MVVM architecture"),
        Triple("rust", "Rust CLI & Core Engine", "Cargo workspace, memory safe systems code")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = OpenCodeCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Workspace",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Autonomous Payments Service") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        unfocusedBorderColor = OpenCodeBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Microservice with stripe webhooks") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OpenCodeCyan,
                        unfocusedBorderColor = OpenCodeBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT TEMPLATE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                templates.forEach { (key, title, subtitle) ->
                    val isSelected = selectedTemplate == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) OpenCodeCyan.copy(alpha = 0.15f) else OpenCodeSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) OpenCodeCyan else OpenCodeBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTemplate = key }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OpenCodeCyan else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = subtitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = OpenCodeCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateProject(name, description.ifBlank { "OpenCode autonomous workspace" }, selectedTemplate)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OpenCodeCyan),
                modifier = Modifier.testTag("create_project_confirm_button")
            ) {
                Text("Initialize Workspace", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

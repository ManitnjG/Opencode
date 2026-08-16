package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentMessageEntity
import com.example.ui.theme.DiffAddBackground
import com.example.ui.theme.DiffAddText
import com.example.ui.theme.DiffDeleteBackground
import com.example.ui.theme.DiffDeleteText
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
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AgentMessageCard(
    message: AgentMessageEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    when (message.sender) {
        "USER" -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(OpenCodeCyan.copy(alpha = 0.15f))
                        .border(1.dp, OpenCodeCyan.copy(alpha = 0.35f), RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(OpenCodeCyan.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User",
                                    tint = OpenCodeCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Developer Prompt",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OpenCodeCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message.content,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        "SYSTEM" -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OpenCodeSurfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, OpenCodeBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "System",
                        tint = OpenCodeAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.content,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        "AGENT" -> {
            var reasoningExpanded by remember { mutableStateOf(false) }
            var toolsExpanded by remember { mutableStateOf(true) }

            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("agent_message_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OpenCodeSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header: OpenCode Agent badge + Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OpenCodePurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Agent",
                                    tint = OpenCodePurple,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OpenCode Agent",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OpenCodePurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Autonomous Execution",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(OpenCodeSurfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Agent Response", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Collapsible Reasoning Process Drawer
                    if (!message.reasoning.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(OpenCodeSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, OpenCodeBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reasoningExpanded = !reasoningExpanded }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Reasoning",
                                        tint = OpenCodeAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Agent Reasoning Chain",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OpenCodeAmber
                                    )
                                }
                                Icon(
                                    imageVector = if (reasoningExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            AnimatedVisibility(visible = reasoningExpanded) {
                                Text(
                                    text = message.reasoning,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)
                                )
                            }
                        }
                    }

                    // Tool Execution Cards
                    if (!message.toolCallsJson.isNullOrBlank()) {
                        val tools = remember(message.toolCallsJson) {
                            try {
                                val arr = JSONArray(message.toolCallsJson)
                                (0 until arr.length()).map { arr.getJSONObject(it) }
                            } catch (e: Exception) {
                                emptyList<JSONObject>()
                            }
                        }

                        if (tools.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OpenCodeSurfaceElevated)
                                    .border(1.dp, OpenCodeBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toolsExpanded = !toolsExpanded },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Terminal,
                                            contentDescription = null,
                                            tint = OpenCodeMint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tool Executions (${tools.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OpenCodeMint
                                        )
                                    }
                                    Icon(
                                        imageVector = if (toolsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AnimatedVisibility(visible = toolsExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        tools.forEach { tool ->
                                            val name = tool.optString("tool", "tool")
                                            val input = tool.optString("input", "")
                                            val output = tool.optString("output", "")
                                            val duration = tool.optString("duration", "")

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(OpenCodeSurfaceVariant)
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Success",
                                                    tint = OpenCodeMint,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = OpenCodeCyan,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = input,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (duration.isNotEmpty()) {
                                                    Text(
                                                        text = duration,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Diff Snapshot Card
                    if (!message.diffSnapshotJson.isNullOrBlank()) {
                        val diffObj = remember(message.diffSnapshotJson) {
                            try { JSONObject(message.diffSnapshotJson) } catch (e: Exception) { null }
                        }

                        if (diffObj != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DiffPreviewCard(
                                fileName = diffObj.optString("file", "workspace_file"),
                                additions = diffObj.optInt("additions", 0),
                                deletions = diffObj.optInt("deletions", 0),
                                diffText = diffObj.optString("diff", "")
                            )
                        }
                    }

                    // Main Content Render
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DiffPreviewCard(
    fileName: String,
    additions: Int,
    deletions: Int,
    diffText: String,
    modifier: Modifier = Modifier
) {
    var isDiffExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(OpenCodeSurfaceVariant)
            .border(1.dp, OpenCodeBorder, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDiffExpanded = !isDiffExpanded }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = OpenCodeCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = fileName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+$additions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiffAddText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "-$deletions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DiffDeleteText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isDiffExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = isDiffExpanded) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12))
                    .horizontalScroll(scrollState)
                    .padding(8.dp)
            ) {
                diffText.lines().take(20).forEach { line ->
                    val isAdd = line.startsWith("+")
                    val isDel = line.startsWith("-")
                    val isHeader = line.startsWith("@") || line.startsWith("---") || line.startsWith("+++")

                    val lineBg = when {
                        isAdd -> DiffAddBackground
                        isDel -> DiffDeleteBackground
                        else -> Color.Transparent
                    }

                    val lineTextColor = when {
                        isAdd -> DiffAddText
                        isDel -> DiffDeleteText
                        isHeader -> OpenCodeCyan
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lineBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = line,
                            style = MonospaceCodeStyle.copy(
                                fontSize = 11.sp,
                                color = lineTextColor
                            )
                        )
                    }
                }
            }
        }
    }
}

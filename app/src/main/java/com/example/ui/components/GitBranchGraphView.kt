package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitSnapshotEntity
import com.example.ui.theme.DiffAddText
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GitBranchGraphView(
    snapshots: List<GitSnapshotEntity>,
    selectedSnapshot: GitSnapshotEntity?,
    onSelectSnapshot: (GitSnapshotEntity) -> Unit,
    onRequestRollback: (GitSnapshotEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val branchColors = listOf(
        OpenCodeCyan,
        OpenCodePurple,
        OpenCodeMint,
        OpenCodeAmber
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Branch status header bar
        Surface(
            color = OpenCodeSurface,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OpenCodeBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = null,
                        tint = OpenCodeCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Branch: ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "main",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = OpenCodeCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(OpenCodeMint.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("HEAD -> 0.1.0", fontSize = 9.sp, color = OpenCodeMint, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text(
                            text = "${snapshots.size} commits tracked in repository history",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Graph Timeline List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(snapshots, key = { _, s -> s.id }) { index, snapshot ->
                val isSelected = snapshot.id == selectedSnapshot?.id
                val isLatest = index == 0
                val isFirst = index == snapshots.size - 1
                val nodeColor = branchColors[index % branchColors.size]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSnapshot(snapshot) }
                ) {
                    // Visual Git Node and Line Canvas Column
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2

                            // Top connector line
                            if (!isLatest) {
                                drawLine(
                                    color = OpenCodeBorder,
                                    start = Offset(centerX, 0f),
                                    end = Offset(centerX, centerY),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            // Bottom connector line
                            if (!isFirst) {
                                drawLine(
                                    color = OpenCodeBorder,
                                    start = Offset(centerX, centerY),
                                    end = Offset(centerX, size.height),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            // Center Commit Node Circle
                            drawCircle(
                                color = if (isSelected) Color.White else nodeColor,
                                radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                            drawCircle(
                                color = nodeColor,
                                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                        }
                    }

                    // Commit Details Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OpenCodeSurfaceElevated else OpenCodeSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) OpenCodeCyan else OpenCodeBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = snapshot.commitHash,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = nodeColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isLatest) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(OpenCodeCyan.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("HEAD", fontSize = 9.sp, color = OpenCodeCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(snapshot.timestamp))
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = snapshot.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${snapshot.filesChanged} files",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+${snapshot.additions}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = DiffAddText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "-${snapshot.deletions}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = DiffDeleteText
                                    )
                                }

                                if (!isLatest) {
                                    OutlinedButton(
                                        onClick = { onRequestRollback(snapshot) },
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = OpenCodeAmber, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Rollback", fontSize = 10.sp, color = OpenCodeAmber)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

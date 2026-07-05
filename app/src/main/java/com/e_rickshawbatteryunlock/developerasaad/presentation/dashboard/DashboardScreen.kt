package com.e_rickshawbatteryunlock.developerasaad.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryInfo
import com.e_rickshawbatteryunlock.developerasaad.domain.model.BatteryStatus
import com.e_rickshawbatteryunlock.developerasaad.domain.model.ConnectionState
import com.e_rickshawbatteryunlock.developerasaad.domain.model.RecoveryCapability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToRecovery: () -> Unit,
    onNavigateToPassword: () -> Unit,
    onDisconnected: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigateBack by viewModel.navigateBack.collectAsState()

    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            viewModel.onNavigatedBack()
            onDisconnected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Battery Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        ConnectionStateLine(uiState.connectionState)
                    }
                },
                actions = {
                    // Disconnect button
                    OutlinedButton(
                        onClick = viewModel::onDisconnect,
                        enabled = !uiState.isDisconnecting,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Disconnect",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Disconnect", style = MaterialTheme.typography.labelMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // State of charge hero card
            SocHeroCard(batteryInfo = uiState.batteryInfo)

            // Metrics grid
            MetricsGrid(batteryInfo = uiState.batteryInfo)

            // Cell voltages (if available)
            if (uiState.batteryInfo.cellVoltagesMillivolts.isNotEmpty()) {
                CellVoltagesCard(cells = uiState.batteryInfo.cellVoltagesMillivolts)
            }

            // Action buttons
            ActionButtons(
                capability = uiState.capability,
                onRecovery = onNavigateToRecovery,
                onPassword = onNavigateToPassword,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConnectionStateLine(state: ConnectionState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    val color by animateColorAsState(
        targetValue = when (state) {
            ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
            ConnectionState.CONNECTING,
            ConnectionState.DISCOVERING_SERVICES -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        },
        label = "connection_color",
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (state == ConnectionState.CONNECTED) 1f else alpha)
                .background(color = color, shape = CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = when (state) {
                ConnectionState.CONNECTED -> "Connected"
                ConnectionState.CONNECTING -> "Connecting…"
                ConnectionState.DISCOVERING_SERVICES -> "Discovering services…"
                ConnectionState.DISCONNECTED -> "Disconnected"
                ConnectionState.FAILED -> "Connection failed"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
private fun SocHeroCard(batteryInfo: BatteryInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.BatteryChargingFull,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = batteryInfo.stateOfChargePercent?.let { "$it%" } ?: "—",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "State of Charge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                batteryInfo.status.let { status ->
                    if (status != BatteryStatus.NORMAL && status != BatteryStatus.UNKNOWN) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "⚠ ${status.name.replace("_", " ")}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(batteryInfo: BatteryInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bolt,
                label = "Voltage",
                value = batteryInfo.packVoltageMillivolts?.let {
                    "%.1fV".format(it / 1000.0)
                } ?: "N/A",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ElectricRickshaw,
                label = "Current",
                value = batteryInfo.currentMilliamps?.let {
                    if (it >= 0) "+%.1fA".format(it / 1000.0)
                    else "%.1fA".format(it / 1000.0)
                } ?: "N/A",
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat,
                label = "Temperature",
                value = batteryInfo.temperatures.firstOrNull()?.let {
                    "%.1f°C".format(it / 10.0)
                } ?: "N/A",
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.BatteryChargingFull,
                label = "Capacity",
                value = batteryInfo.remainingCapacityMAh?.let {
                    val full = batteryInfo.fullCapacityMAh
                    if (full != null) "${it / 1000}/${full / 1000} Ah"
                    else "${it / 1000} Ah"
                } ?: "N/A",
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CellVoltagesCard(cells: List<Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cell Voltages (${cells.size} cells)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            cells.chunked(4).forEachIndexed { rowIdx, rowCells ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    rowCells.forEachIndexed { colIdx, mv ->
                        val cellNumber = rowIdx * 4 + colIdx + 1
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.3fV".format(mv / 1000.0),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "C$cellNumber",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (rowIdx < cells.size / 4) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActionButtons(
    capability: RecoveryCapability,
    onRecovery: () -> Unit,
    onPassword: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (capability.canEnableDischarge) {
            Button(
                onClick = onRecovery,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Unlock Battery",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (capability.canSetPassword) {
            FilledTonalButton(
                onClick = onPassword,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Default.Password, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Manage Password",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (!capability.canEnableDischarge && !capability.canSetPassword) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(
                    text = "No control operations are supported by this BMS. " +
                        "Battery telemetry is available for monitoring only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

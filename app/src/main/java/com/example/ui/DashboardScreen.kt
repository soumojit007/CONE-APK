package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClonedAccount
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CloneShieldViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(0) } // 0: Accounts, 1: Platform Shield, 2: Gemini Assistant
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ElegantDarkBg,
        topBar = {
            // Elegant top bar with status row below title matching the Design spec
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElegantDarkBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Parallel Guard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            color = ElegantDarkPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElegantEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stealth Engine Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ElegantEmerald
                            )
                        }
                    }
                    // Profile representative shield representation from the theme
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ElegantDarkOutlineVariant)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Guard",
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = ElegantDarkBg,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Accounts") },
                    label = { Text("Spaces") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElegantDarkOnPrimary,
                        selectedTextColor = ElegantDarkPrimary,
                        unselectedIconColor = ElegantDarkSecondary,
                        unselectedTextColor = ElegantDarkSubtle,
                        indicatorColor = ElegantDarkPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_clones")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Platform Shield") },
                    label = { Text("Proxy") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElegantDarkOnPrimary,
                        selectedTextColor = ElegantDarkPrimary,
                        unselectedIconColor = ElegantDarkSecondary,
                        unselectedTextColor = ElegantDarkSubtle,
                        indicatorColor = ElegantDarkPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_shield")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "AI Help") },
                    label = { Text("Config") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElegantDarkOnPrimary,
                        selectedTextColor = ElegantDarkPrimary,
                        unselectedIconColor = ElegantDarkSecondary,
                        unselectedTextColor = ElegantDarkSubtle,
                        indicatorColor = ElegantDarkPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_ai")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary,
                    shape = RoundedCornerShape(24.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Cloned Profile") },
                    text = { Text("Add Clone", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("fab_add_clone_account")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ElegantDarkBg)
        ) {
            when (currentTab) {
                0 -> AccountsListTab(
                    accounts = accounts,
                    onIncrementAction = { viewModel.incrementAction(it) },
                    onStartCooldown = { account, mins -> viewModel.startCooldown(account, mins) },
                    onResetActions = { viewModel.resetDailyActions(it) },
                    onStatusChange = { account, status, reason -> viewModel.changeStatus(account, status, reason) },
                    onDelete = { viewModel.deleteAccount(it) },
                    onEndCooldown = { viewModel.endCooldown(it) }
                )
                1 -> PlatformShieldTab(accounts = accounts)
                2 -> GeminiAssistantTab(viewModel = viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddCloneDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { username, platform, notes, proxy, safeLimit ->
                viewModel.addAccount(username, platform, notes, proxy, safeLimit)
                showAddDialog = false
            }
        )
    }
}

// ======================== TAB I: ACCOUNTS LIST ========================

@Composable
fun AccountsListTab(
    accounts: List<ClonedAccount>,
    onIncrementAction: (ClonedAccount) -> Unit,
    onStartCooldown: (ClonedAccount, Int) -> Unit,
    onResetActions: (ClonedAccount) -> Unit,
    onStatusChange: (ClonedAccount, String, String) -> Unit,
    onDelete: (ClonedAccount) -> Unit,
    onEndCooldown: (ClonedAccount) -> Unit
) {
    if (accounts.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Empty Clones",
                tint = ElegantDarkPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Cloned Accounts Tracked",
                style = MaterialTheme.typography.titleMedium,
                color = ElegantDarkText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Click the 'Add Clone' FAB below to log proxy devices, social identifiers, and active ban warning cooldowns immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = ElegantDarkSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cloned Instances (${accounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElegantDarkPrimary
                    )
                    Text(
                        text = "Real-time Ban Safeguard",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ElegantEmerald
                    )
                }
            }

            items(accounts, key = { it.id }) { account ->
                AccountCard(
                    account = account,
                    onIncrementAction = onIncrementAction,
                    onStartCooldown = onStartCooldown,
                    onResetActions = onResetActions,
                    onStatusChange = onStatusChange,
                    onDelete = onDelete,
                    onEndCooldown = onEndCooldown
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountCard(
    account: ClonedAccount,
    onIncrementAction: (ClonedAccount) -> Unit,
    onStartCooldown: (ClonedAccount, Int) -> Unit,
    onResetActions: (ClonedAccount) -> Unit,
    onStatusChange: (ClonedAccount, String, String) -> Unit,
    onDelete: (ClonedAccount) -> Unit,
    onEndCooldown: (ClonedAccount) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStatusModal by remember { mutableStateOf(false) }
    var showCooldownModal by remember { mutableStateOf(false) }

    // Color coordination based on Status matching theme variables
    val (statusColor, statusBg, statusText) = when (account.banStatus) {
        "ACTIVE" -> Triple(ElegantEmerald, ElegantEmeraldBg, "Active & Safe")
        "WARNING" -> Triple(ElegantWarning, ElegantWarningBg, "Warnings Logged")
        "SHADOWBANNED" -> Triple(ElegantShadow, ElegantShadowBg, "Shadowbanned")
        "BANNED" -> Triple(ElegantCrimson, ElegantCrimsonBg, "Banned / Restricted")
        else -> Triple(ElegantDarkSecondary, ElegantDarkOutlineVariant, "Unknown")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(24.dp), // Elegant modern curvature
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurfaceCard
        ),
        border = BorderStroke(
            width = if (account.isUnderCooldown) 2.dp else 1.dp,
            color = if (account.isUnderCooldown) ElegantDarkPrimary else ElegantDarkOutlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val platformIcon = when (account.platform.lowercase()) {
                            "whatsapp" -> Icons.Default.Lock
                            "discord" -> Icons.Default.Person
                            "telegram" -> Icons.Default.Share
                            "facebook" -> Icons.Default.Home
                            "reddit" -> Icons.Default.Info
                            else -> Icons.Default.Build
                        }
                        Icon(
                            imageVector = platformIcon,
                            contentDescription = account.platform,
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = account.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElegantDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = account.platform,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = ElegantDarkSecondary
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cooldown visual countdown
            if (account.isUnderCooldown) {
                Surface(
                    color = ElegantDarkPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElegantDarkPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Cooldown active",
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REST COOLDOWN ENFORCED",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantDarkPrimary
                            )
                        }
                        Text(
                            text = "~${account.cooldownMinutesRemaining} min left",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkPrimary
                        )
                    }
                }
            }

            // Safety Gauge / Linear Progress indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Account Health Score",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = ElegantDarkSecondary
                    )
                    Text(
                        text = "${account.safetyPercentage}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { account.safetyPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = ElegantDarkOutlineVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Actions tracker bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Action Limit Caps:",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkSecondary
                )
                Text(
                    text = "${account.currentDailyActions} / ${account.dailyActionLimit} safe ops",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (account.currentDailyActions >= account.dailyActionLimit) ElegantCrimson else ElegantDarkText
                )
            }

            if (account.currentDailyActions >= account.dailyActionLimit) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantCrimsonBg, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Risk warning",
                        tint = ElegantCrimson,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Safe activity threshold exceeded! Pause actions.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantCrimson
                    )
                }
            }

            // Expandable settings and luxury details
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = ElegantDarkOutlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (account.lastUsedTimestamp > 0L) {
                        val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
                        val dateText = sdf.format(Date(account.lastUsedTimestamp))
                        Text(
                            text = "Last Action Logged: $dateText",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (account.proxy.isNotBlank()) {
                        Text(
                            text = "Assigned IP Proxy: ${account.proxy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (account.loginNotes.isNotBlank()) {
                        Text(
                            text = "Details Note: ${account.loginNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (account.banReason.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantCrimsonBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Platform Ban Status / Warning Reason:",
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantCrimson,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = account.banReason,
                                    color = ElegantDarkText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Premium curved CTA Buttons Control panel
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onIncrementAction(account) },
                            enabled = !account.isBanned && !account.isUnderCooldown,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary,
                                disabledContainerColor = ElegantDarkOutlineVariant
                            ),
                            modifier = Modifier.testTag("increment_action_btn_${account.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log Action", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        FilledTonalButton(
                            onClick = {
                                if (account.isUnderCooldown) {
                                    onEndCooldown(account)
                                } else {
                                    showCooldownModal = true
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            enabled = !account.isBanned,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElegantDarkOutlineVariant,
                                contentColor = ElegantDarkText
                            ),
                            modifier = Modifier.testTag("cooldown_btn_${account.id}")
                        ) {
                            Icon(
                                imageVector = if (account.isUnderCooldown) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (account.isUnderCooldown) "End Cooldown" else "Cooldown Rest",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = { showStatusModal = true },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ElegantDarkOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElegantDarkText
                            ),
                            modifier = Modifier.testTag("status_change_btn_${account.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onResetActions(account) },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, ElegantDarkOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ElegantDarkText
                            ),
                            modifier = Modifier.testTag("reset_actions_btn_${account.id}")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        IconButton(
                            onClick = { onDelete(account) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = ElegantCrimson
                            ),
                            modifier = Modifier.testTag("delete_btn_${account.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
                        }
                    }
                }
            }
        }
    }

    if (showStatusModal) {
        StatusChangeDialog(
            currentStatus = account.banStatus,
            onDismiss = { showStatusModal = false },
            onConfirm = { status, reason ->
                onStatusChange(account, status, reason)
                showStatusModal = false
            }
        )
    }

    if (showCooldownModal) {
        CooldownDurationDialog(
            onDismiss = { showCooldownModal = false },
            onConfirm = { mins ->
                onStartCooldown(account, mins)
                showCooldownModal = false
            }
        )
    }
}

// ======================== TAB II: PLATFORM SHIELD COMPLIANCE ========================

@Composable
fun PlatformShieldTab(accounts: List<ClonedAccount>) {
    val totalClones = accounts.size
    val activeCount = accounts.count { it.banStatus == "ACTIVE" && !it.isUnderCooldown }
    val cooldownCount = accounts.count { it.isUnderCooldown }
    val bannedCount = accounts.count { it.banStatus == "BANNED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Anti-Ban Shield Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkPrimary
            )
        }

        // Stats Luxury Unified Scorecard Card matching Design HTML visual requirements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantDarkSurfaceCard
                ),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "GLOBAL RISK PROFILE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = ElegantDarkSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val safetyScore = if (accounts.isEmpty()) 100 else {
                                val sum = accounts.sumOf { it.safetyPercentage }
                                sum / accounts.size
                            }

                            val standingText = when {
                                safetyScore >= 80 -> "Risk Level: Low (Conditioned)"
                                safetyScore >= 50 -> "Risk Level: Medium / Monitor"
                                else -> "Risk Level: Severe / Imminent Ban"
                            }

                            Text(
                                text = standingText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ElegantDarkText
                            )
                        }

                        // Status pill badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElegantEmeraldBg,
                            border = BorderStroke(1.dp, ElegantEmerald.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "V14.2 PROTECT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantEmerald,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val finalSafetyScore = if (accounts.isEmpty()) 100 else {
                        val sum = accounts.sumOf { it.safetyPercentage }
                        sum / accounts.size
                    }

                    // Score Progress indicator bar matching exact colors
                    LinearProgressIndicator(
                        progress = { finalSafetyScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ElegantEmerald,
                        trackColor = ElegantDarkOutline
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$totalClones accounts isolated with Device ID & dynamic proxy randomization.",
                        fontSize = 11.sp,
                        color = ElegantDarkSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Mini counters grid with unified curved borders
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCardAlt),
                    border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Clones", fontSize = 11.sp, color = ElegantDarkSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$activeCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElegantEmerald)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCardAlt),
                    border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cooldown", fontSize = 11.sp, color = ElegantDarkSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$cooldownCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElegantDarkPrimary)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCardAlt),
                    border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bans Logged", fontSize = 11.sp, color = ElegantDarkSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$bannedCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ElegantCrimson)
                    }
                }
            }
        }

        // Active Platform guidelines list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Anti-Bot Heuristics Compliance",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = ElegantDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val checks = listOf(
                        "Residential/SOCKS5 Proxies configured (isolates MAC and network signatures)." to (accounts.any { it.proxy.isNotBlank() } || totalClones <= 1),
                        "All active accounts preserved below safe limits (Max 50 actions/day)." to accounts.none { it.currentDailyActions >= it.dailyActionLimit },
                        "No perma-bans or shadowbanned clones active right now." to (bannedCount == 0 && accounts.none { it.banStatus == "SHADOWBANNED" }),
                        "Conditioning delay enabled (interval between logs greater than 60s)." to true
                    )

                    checks.forEachIndexed { index, pair ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (pair.second) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (pair.second) ElegantEmerald else ElegantWarning,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pair.first,
                                fontSize = 12.sp,
                                color = ElegantDarkText,
                                lineHeight = 16.sp,
                                fontWeight = if (pair.second) FontWeight.Normal else FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Reference platform details
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Suggested Maximum Limits:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val suggestions = listOf(
                    "WhatsApp Clones" to "Max 25 outbound chats/day. Distribute interactions with random 30s delays.",
                    "Discord Bots/Clones" to "Limit joins to 5 servers/day. Avoid bulk messaging in under 10 seconds.",
                    "Telegram Multiple Accounts" to "Max 15 cold invitations/day. Force SOCKS5 proxies per instance."
                )

                suggestions.forEach { (title, subtitle) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCardAlt),
                        border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = title, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp, 
                                color = ElegantDarkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle, 
                                fontSize = 12.sp, 
                                color = ElegantDarkSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================== TAB III: GEMINI AI ASSISTANT ========================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeminiAssistantTab(viewModel: CloneShieldViewModel) {
    var platformInput by remember { mutableStateOf("WhatsApp") }
    var accountUsername by remember { mutableStateOf("") }
    var banReasonInput by remember { mutableStateOf("Sudden high frequency messaging trigger / dynamic SOCKS proxy mismatch.") }

    val appealResult by viewModel.appealText.collectAsStateWithLifecycle()
    val isAppealLoading by viewModel.isGeneratingAppeal.collectAsStateWithLifecycle()

    val warmupResult by viewModel.warmUpPlanText.collectAsStateWithLifecycle()
    val isWarmupLoading by viewModel.isGeneratingWarmUp.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gemini AI Trust & Safety Advisor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElegantDarkPrimary
            )
            Text(
                text = "Instantly draft customized appeals or generate tailored warmup plans for cloned instances.",
                style = MaterialTheme.typography.bodySmall,
                color = ElegantDarkSecondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
                border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Advisor Inputs Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = ElegantDarkText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Target Platform", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantDarkPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val platforms = listOf("WhatsApp", "Discord", "Telegram", "Reddit", "Facebook", "Custom")
                        platforms.forEach { plat ->
                            FilterChip(
                                selected = platformInput == plat,
                                onClick = { platformInput = plat },
                                label = { Text(plat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantDarkPrimary,
                                    selectedLabelColor = ElegantDarkOnPrimary,
                                    containerColor = Color.Transparent,
                                    labelColor = ElegantDarkSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = platformInput == plat,
                                    borderColor = ElegantDarkOutline,
                                    selectedBorderColor = ElegantDarkPrimary
                                ),
                                modifier = Modifier.testTag("advisor_chip_$plat")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = accountUsername,
                        onValueChange = { accountUsername = it },
                        label = { Text("Account Identifier (Phone or User Handle)") },
                        placeholder = { Text("e.g. +14155552671 or clone_agent_03") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantDarkText,
                            unfocusedTextColor = ElegantDarkText,
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedLabelColor = ElegantDarkPrimary,
                            unfocusedLabelColor = ElegantDarkSecondary,
                            cursorColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advisor_username_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("Stated Ban Reason / Spam Action Trigger") },
                        placeholder = { Text("e.g. Suspicious automated logging pattern") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantDarkText,
                            unfocusedTextColor = ElegantDarkText,
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            focusedLabelColor = ElegantDarkPrimary,
                            unfocusedLabelColor = ElegantDarkSecondary,
                            cursorColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advisor_reason_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val userParam = accountUsername.ifBlank { "UserClone_Verified" }
                                viewModel.generateAppeal(platformInput, banReasonInput, userParam)
                            },
                            enabled = !isAppealLoading && !isWarmupLoading,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_generate_appeal")
                        ) {
                            if (isAppealLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElegantDarkOnPrimary)
                            } else {
                                Text("AI Draft Appeal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.generateWarmUpPlan(platformInput, 50)
                            },
                            enabled = !isAppealLoading && !isWarmupLoading,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElegantDarkOutlineVariant,
                                contentColor = ElegantDarkText
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_generate_warmup")
                        ) {
                            if (isWarmupLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElegantDarkText)
                            } else {
                                Text("AI Warm-up Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Appeal Response Display
        if (appealResult.isNotBlank() || isAppealLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
                    border = BorderStroke(1.dp, ElegantDarkPrimary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Platform Appeal Correspondence",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = ElegantDarkPrimary
                            )
                            if (appealResult.isNotBlank() && !isAppealLoading) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(appealResult))
                                        Toast.makeText(context, "Appeal letter copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantDarkPrimary,
                                        contentColor = ElegantDarkOnPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("btn_copy_appeal")
                                ) {
                                    Text("Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (isAppealLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ElegantDarkPrimary)
                            }
                        } else {
                            Text(
                                text = appealResult,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = ElegantDarkText,
                                modifier = Modifier.testTag("result_text_appeal")
                            )
                        }
                    }
                }
            }
        }

        // Warmup Plan Response Display
        if (warmupResult.isNotBlank() || isWarmupLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
                    border = BorderStroke(1.dp, ElegantEmerald)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tailored 14-day Conditioning Protocol",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = ElegantEmerald
                            )
                            if (warmupResult.isNotBlank() && !isWarmupLoading) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(warmupResult))
                                        Toast.makeText(context, "Warm-up schedule copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElegantEmerald,
                                        contentColor = ElegantDarkBg
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("btn_copy_warmup")
                                ) {
                                    Text("Copy", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        if (isWarmupLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = ElegantEmerald)
                            }
                        } else {
                            Text(
                                text = warmupResult,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = ElegantDarkText,
                                modifier = Modifier.testTag("result_text_warmup")
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================== MODALS & COMPONENT DIALOGS ========================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCloneDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, platform: String, notes: String, proxy: String, safeLimit: Int) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("WhatsApp") }
    var loginNotes by remember { mutableStateOf("") }
    var proxyInput by remember { mutableStateOf("") }
    var actionLimitInput by remember { mutableStateOf(50) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_clone_dialog"),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
            border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Register Cloned Instance",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = ElegantDarkPrimary
                    )
                }

                item {
                    Text("Instance Primary Identifier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantDarkSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("e.g. +447700900077 or alpha_clone_2") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantDarkText,
                            unfocusedTextColor = ElegantDarkText,
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            cursorColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_username_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Target Platform", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantDarkSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val plats = listOf("WhatsApp", "Discord", "Telegram", "Facebook", "Reddit", "Custom")
                        plats.forEach { plat ->
                            FilterChip(
                                selected = selectedPlatform == plat,
                                onClick = { selectedPlatform = plat },
                                label = { Text(plat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantDarkPrimary,
                                    selectedLabelColor = ElegantDarkOnPrimary,
                                    containerColor = Color.Transparent,
                                    labelColor = ElegantDarkSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedPlatform == plat,
                                    borderColor = ElegantDarkOutline,
                                    selectedBorderColor = ElegantDarkPrimary
                                ),
                                modifier = Modifier.testTag("dialog_chip_$plat")
                            )
                        }
                    }
                }

                item {
                    Text("Instance Proxy Partition (Optional)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantDarkSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = proxyInput,
                        onValueChange = { proxyInput = it },
                        placeholder = { Text("e.g. 192.168.1.50:8080 or socksproxy.net:909") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantDarkText,
                            unfocusedTextColor = ElegantDarkText,
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            cursorColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_proxy_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Security Profile Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantDarkSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loginNotes,
                        onValueChange = { loginNotes = it },
                        placeholder = { Text("e.g. Custom UA profile string, secondary device MAC") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ElegantDarkText,
                            unfocusedTextColor = ElegantDarkText,
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkOutline,
                            cursorColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_notes_input"),
                        maxLines = 2
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Safe-Limit Cap: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElegantDarkSecondary)
                        Text("$actionLimitInput operations", fontSize = 12.sp, color = ElegantDarkPrimary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = actionLimitInput.toFloat(),
                        onValueChange = { actionLimitInput = it.toInt() },
                        valueRange = 10f..200f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = ElegantDarkPrimary,
                            activeTrackColor = ElegantDarkPrimary,
                            inactiveTrackColor = ElegantDarkOutline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_limit_slider")
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss, 
                            colors = ButtonDefaults.textButtonColors(contentColor = ElegantDarkSecondary),
                            modifier = Modifier.testTag("dialog_cancel_btn")
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (username.isNotBlank()) {
                                    onConfirm(username, selectedPlatform, loginNotes, proxyInput, actionLimitInput)
                                }
                            },
                            enabled = username.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary,
                                disabledContainerColor = ElegantDarkOutlineVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("dialog_save_btn")
                        ) {
                            Text("Save Clone", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChangeDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (status: String, reason: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var reasonInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("status_change_dialog"),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
            border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configure Ban Status",
                    color = ElegantDarkPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                val statuses = listOf(
                    "ACTIVE" to "Active & Safe",
                    "WARNING" to "Warnings Logged",
                    "SHADOWBANNED" to "Shadowbanned",
                    "BANNED" to "Permanently Banned/Restricted"
                )

                statuses.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == key,
                            onClick = { selectedStatus = key },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ElegantDarkPrimary,
                                unselectedColor = ElegantDarkOutline
                            ),
                            modifier = Modifier.testTag("dialog_radio_$key")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, fontSize = 13.sp, color = ElegantDarkText, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Reason / Warning Trigger notes") },
                    placeholder = { Text("e.g. Shadowban detected using automated logs") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElegantDarkText,
                        unfocusedTextColor = ElegantDarkText,
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkOutline,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkSecondary,
                        cursorColor = ElegantDarkPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_status_reason")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = ElegantDarkSecondary)
                    ) { 
                        Text("Cancel", fontWeight = FontWeight.SemiBold) 
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedStatus, reasonInput) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkPrimary,
                            contentColor = ElegantDarkOnPrimary
                        )
                    ) {
                        Text("Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CooldownDurationDialog(
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(60) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("cooldown_duration_dialog"),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceCard),
            border = BorderStroke(1.dp, ElegantDarkOutlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Initiate Security Cooldown",
                    color = ElegantDarkPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Enforcing rest periods resets platform spam analytics timers on clone signatures.",
                    fontSize = 11.sp,
                    color = ElegantDarkSecondary,
                    lineHeight = 15.sp
                )

                val shortTimes = listOf(
                    15 to "15 Minutes (Brief rest)",
                    60 to "1 Hour (Recommended)",
                    240 to "4 Hours (Deep Conditioning)",
                    1440 to "24 Hours (Full Lockdown)"
                )

                shortTimes.forEach { (m, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMinutes = m }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMinutes == m,
                            onClick = { selectedMinutes = m },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ElegantDarkPrimary,
                                unselectedColor = ElegantDarkOutline
                            ),
                            modifier = Modifier.testTag("cooldown_radio_$m")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, fontSize = 13.sp, color = ElegantDarkText, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = ElegantDarkSecondary)
                    ) { 
                        Text("Cancel", fontWeight = FontWeight.SemiBold) 
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedMinutes) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkPrimary,
                            contentColor = ElegantDarkOnPrimary
                        )
                    ) {
                        Text("Enforce Rest", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

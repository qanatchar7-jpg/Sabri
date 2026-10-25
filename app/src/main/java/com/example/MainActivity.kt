package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BillingViewModel
import com.example.ui.components.AboutDialog
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppTab {
    BILLING,
    CATALOG,
    HISTORY
}

class MainActivity : ComponentActivity() {
    private val billingViewModel: BillingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = billingViewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: BillingViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(AppTab.BILLING) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    // If on a secondary tab and system back is pressed, return to billing screen
    BackHandler(enabled = currentTab != AppTab.BILLING) {
        currentTab = AppTab.BILLING
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAboutDialog = true }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "ScanFacture",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("dev_credit_pill")
                        ) {
                            Text(
                                text = "Dev: O.SABRI • v1.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleSound() },
                            modifier = Modifier.testTag("toggle_sound_button")
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (isSoundEnabled) "Désactiver le bip sonore" else "Activer le bip sonore",
                                tint = if (isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.testTag("about_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Informations sur le développeur et version",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.BILLING,
                    onClick = { currentTab = AppTab.BILLING },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.BILLING) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale,
                            contentDescription = "Caisse & Facturation"
                        )
                    },
                    label = { Text("Facturation") },
                    modifier = Modifier.testTag("tab_billing")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CATALOG,
                    onClick = { currentTab = AppTab.CATALOG },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CATALOG) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                            contentDescription = "Catalogue & Prix"
                        )
                    },
                    label = { Text("Articles & Prix") },
                    modifier = Modifier.testTag("tab_catalog")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { currentTab = AppTab.HISTORY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "Historique des Factures"
                        )
                    },
                    label = { Text("Historique") },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.BILLING -> BillingScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.CATALOG -> CatalogScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.HISTORY -> HistoryScreen(
                viewModel = viewModel,
                onNavigateToBilling = { currentTab = AppTab.BILLING },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

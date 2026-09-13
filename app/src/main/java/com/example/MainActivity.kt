package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppTab
import com.example.ui.components.BazaarBottomNavigation
import com.example.ui.components.FloatingVoiceMicButton
import com.example.ui.components.VoiceTransactionDialog
import com.example.ui.localization.LocalizedStrings
import com.example.ui.screens.AIAssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BazaarViewModel

enum class RootNavState {
    ONBOARDING,
    LOGIN,
    REGISTER,
    MAIN_APP,
    AI_ASSISTANT,
    REPORTS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: BazaarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                BazaarSaathiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BazaarSaathiApp(viewModel: BazaarViewModel) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val vendorProfile by viewModel.vendorProfile.collectAsState()

    val transactions by viewModel.transactions.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val inventory by viewModel.inventory.collectAsState()

    val todaySummary by viewModel.todaySummary.collectAsState()
    val cashflowPrediction by viewModel.cashflowPrediction.collectAsState()
    val inventoryPrediction by viewModel.inventoryPrediction.collectAsState()
    val businessInsights by viewModel.businessInsights.collectAsState()
    val financialReadiness by viewModel.financialReadiness.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsState()
    val voiceCandidateCommand by viewModel.voiceCandidateCommand.collectAsState()

    val strings = LocalizedStrings.get(currentLanguage)

    var rootNavState by remember(isOnboardingCompleted) {
        mutableStateOf(if (isOnboardingCompleted) RootNavState.MAIN_APP else RootNavState.ONBOARDING)
    }

    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    AnimatedContent(
        targetState = rootNavState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "root_navigation"
    ) { state ->
        when (state) {
            RootNavState.ONBOARDING -> {
                OnboardingScreen(
                    strings = strings,
                    onFinish = {
                        viewModel.completeOnboarding()
                        rootNavState = RootNavState.MAIN_APP
                    }
                )
            }

            RootNavState.LOGIN -> {
                LoginScreen(
                    strings = strings,
                    onLoginSuccess = { profile ->
                        viewModel.updateProfile(profile)
                        rootNavState = RootNavState.MAIN_APP
                    },
                    onNavigateToRegister = { rootNavState = RootNavState.REGISTER },
                    onDemoLogin = {
                        viewModel.seedDemoData()
                        rootNavState = RootNavState.MAIN_APP
                    }
                )
            }

            RootNavState.REGISTER -> {
                RegisterScreen(
                    strings = strings,
                    onRegisterSuccess = { profile ->
                        viewModel.updateProfile(profile)
                        rootNavState = RootNavState.MAIN_APP
                    },
                    onNavigateToLogin = { rootNavState = RootNavState.LOGIN }
                )
            }

            RootNavState.AI_ASSISTANT -> {
                AIAssistantScreen(
                    chatMessages = chatMessages,
                    strings = strings,
                    onSendMessage = { query -> viewModel.sendChatMessage(query) },
                    onSpeakMessage = { text -> viewModel.speak(text) },
                    onBack = { rootNavState = RootNavState.MAIN_APP }
                )
            }

            RootNavState.REPORTS -> {
                ReportsScreen(
                    transactions = transactions,
                    expenses = expenses,
                    strings = strings,
                    onBack = { rootNavState = RootNavState.MAIN_APP }
                )
            }

            RootNavState.SETTINGS -> {
                SettingsScreen(
                    vendorProfile = vendorProfile,
                    currentLanguage = currentLanguage,
                    strings = strings,
                    onLanguageSelected = { lang -> viewModel.setLanguage(lang) },
                    onUpdateProfile = { updated -> viewModel.updateProfile(updated) },
                    onLoadDemoData = { viewModel.seedDemoData() },
                    onClearData = { viewModel.clearAllData() },
                    onBack = { rootNavState = RootNavState.MAIN_APP }
                )
            }

            RootNavState.MAIN_APP -> {
                Scaffold(
                    bottomBar = {
                        BazaarBottomNavigation(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            strings = strings
                        )
                    },
                    floatingActionButton = {
                        FloatingVoiceMicButton(
                            onClick = { viewModel.openVoiceDialog() }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            AppTab.HOME -> {
                                DashboardScreen(
                                    vendorProfile = vendorProfile,
                                    todaySummary = todaySummary,
                                    transactions = transactions,
                                    expenses = expenses,
                                    inventoryPrediction = inventoryPrediction,
                                    strings = strings,
                                    onOpenVoiceDialog = { viewModel.openVoiceDialog() },
                                    onNavigateToSales = { selectedTab = AppTab.SALES },
                                    onNavigateToExpenses = { selectedTab = AppTab.EXPENSES },
                                    onNavigateToInventory = { selectedTab = AppTab.INVENTORY },
                                    onNavigateToAssistant = { rootNavState = RootNavState.AI_ASSISTANT },
                                    onNavigateToReports = { rootNavState = RootNavState.REPORTS },
                                    onNavigateToSettings = { rootNavState = RootNavState.SETTINGS }
                                )
                            }

                            AppTab.SALES -> {
                                SalesScreen(
                                    transactions = transactions,
                                    strings = strings,
                                    onAddSale = { amt, method, prod, cat, desc ->
                                        viewModel.addSale(amt, method, prod, cat, desc)
                                    },
                                    onUpdateSale = { sale -> viewModel.updateSale(sale) },
                                    onDeleteSale = { id -> viewModel.deleteSale(id) }
                                )
                            }

                            AppTab.EXPENSES -> {
                                ExpensesScreen(
                                    expenses = expenses,
                                    strings = strings,
                                    onAddExpense = { amt, cat, desc ->
                                        viewModel.addExpense(amt, cat, desc)
                                    },
                                    onUpdateExpense = { exp -> viewModel.updateExpense(exp) },
                                    onDeleteExpense = { id -> viewModel.deleteExpense(id) }
                                )
                            }

                            AppTab.INVENTORY -> {
                                InventoryScreen(
                                    inventory = inventory,
                                    strings = strings,
                                    onAddItem = { name, cat, qty, unit, buy, sell, minS ->
                                        viewModel.addInventoryItem(name, cat, qty, unit, buy, sell, minS)
                                    },
                                    onUpdateItem = { item -> viewModel.updateInventoryItem(item) },
                                    onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
                                    onDeleteItem = { id -> viewModel.deleteInventoryItem(id) }
                                )
                            }

                            AppTab.INSIGHTS -> {
                                InsightsScreen(
                                    cashflowPrediction = cashflowPrediction,
                                    inventoryPrediction = inventoryPrediction,
                                    businessInsights = businessInsights,
                                    financialReadiness = financialReadiness,
                                    strings = strings
                                )
                            }
                        }
                    }
                }

                // Voice Confirmation Dialog (Global overlay strictly obeying verification before saving)
                VoiceTransactionDialog(
                    isOpen = showVoiceDialog,
                    candidateCommand = voiceCandidateCommand,
                    strings = strings,
                    onDismiss = { viewModel.dismissVoiceDialog() },
                    onProcessText = { spoken -> viewModel.processVoiceSpokenText(spoken) },
                    onConfirmCommand = { cmd -> viewModel.confirmVoiceCommand(cmd) },
                    onEditCommand = { cmd -> /* edit supported in dialog */ }
                )
            }
        }
    }
}

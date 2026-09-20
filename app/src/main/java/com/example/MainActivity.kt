package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AccountLogScreen
import com.example.ui.screens.ApkReleaseScreen
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CategoryManagerScreen
import com.example.ui.screens.CommissionScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtScreen
import com.example.ui.screens.EditDashboardScreen
import com.example.ui.screens.HomeScreenWidgetsScreen
import com.example.ui.screens.ReceiptInboxScreen
import com.example.ui.screens.RecordsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodels.DashboardViewModel

enum class MainDestination {
  DASHBOARD,
  ACCOUNT_LOG,
  CATEGORY_MANAGER,
  DEBTS,
  CUSTOMIZE_DASHBOARD,
  COMMISSIONS,
  BUDGETS,
  SUBSCRIPTIONS,
  RECORDS,
  SETTINGS,
  HOME_SCREEN_WIDGETS,
  RECEIPTS_INBOX,
  ASSETS,
  APK_RELEASES
}

class MainActivity : ComponentActivity() {

  private val dashboardViewModel: DashboardViewModel by viewModels {
    DashboardViewModel.provideFactory(this)
  }

  private var destinationState by mutableStateOf(MainDestination.DASHBOARD)
  private var quickAddState by mutableStateOf(false)
  private var quickPicState by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    handleIntent(intent)

    setContent {
      MyApplicationTheme {
        var currentDestination by remember { mutableStateOf(destinationState) }
        var selectedAccountId by remember { mutableStateOf<Long?>(null) }
        val openQuickAdd = quickAddState
        val openQuickPic = quickPicState

        androidx.compose.runtime.LaunchedEffect(destinationState) {
          currentDestination = destinationState
        }

        BackHandler(enabled = currentDestination != MainDestination.DASHBOARD) {
          currentDestination = MainDestination.DASHBOARD
          destinationState = MainDestination.DASHBOARD
          quickPicState = false
        }

        AnimatedContent(
          targetState = currentDestination,
          transitionSpec = {
            if (targetState == MainDestination.DASHBOARD) {
              // Navigating back to Dashboard: slide out to right, fade in
              (slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(animationSpec = tween(300)))
                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(200)))
            } else {
              // Navigating into a section/screen: slide in from right, fade in
              (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)))
                .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(animationSpec = tween(200)))
            }
          },
          label = "ScreenTransition",
          modifier = Modifier.fillMaxSize()
        ) { destination ->
          when (destination) {
            MainDestination.DASHBOARD -> {
              DashboardScreen(
                viewModel = dashboardViewModel,
                initialOpenQuickAdd = openQuickAdd,
                onQuickAddHandled = { quickAddState = false },
                onNavigateToAccountLog = { accountId ->
                  selectedAccountId = accountId
                  currentDestination = MainDestination.ACCOUNT_LOG
                  destinationState = MainDestination.ACCOUNT_LOG
                },
                onNavigateToCategories = {
                  currentDestination = MainDestination.CATEGORY_MANAGER
                  destinationState = MainDestination.CATEGORY_MANAGER
                },
                onNavigateToDebts = {
                  currentDestination = MainDestination.DEBTS
                  destinationState = MainDestination.DEBTS
                },
                onNavigateToCustomize = {
                  currentDestination = MainDestination.CUSTOMIZE_DASHBOARD
                  destinationState = MainDestination.CUSTOMIZE_DASHBOARD
                },
                onNavigateToCommission = {
                  currentDestination = MainDestination.COMMISSIONS
                  destinationState = MainDestination.COMMISSIONS
                },
                onNavigateToSettings = {
                  currentDestination = MainDestination.SETTINGS
                  destinationState = MainDestination.SETTINGS
                },
                onNavigateToBudgets = {
                  currentDestination = MainDestination.BUDGETS
                  destinationState = MainDestination.BUDGETS
                },
                onNavigateToSubscriptions = {
                  currentDestination = MainDestination.SUBSCRIPTIONS
                  destinationState = MainDestination.SUBSCRIPTIONS
                },
                onNavigateToRecords = {
                  currentDestination = MainDestination.RECORDS
                  destinationState = MainDestination.RECORDS
                },
                onNavigateToWidgets = {
                  currentDestination = MainDestination.HOME_SCREEN_WIDGETS
                  destinationState = MainDestination.HOME_SCREEN_WIDGETS
                },
                onNavigateToReceiptInbox = {
                  currentDestination = MainDestination.RECEIPTS_INBOX
                  destinationState = MainDestination.RECEIPTS_INBOX
                },
                onNavigateToAssets = {
                  currentDestination = MainDestination.ASSETS
                  destinationState = MainDestination.ASSETS
                }
              )
            }
            MainDestination.ACCOUNT_LOG -> {
              AccountLogScreen(
                accountId = selectedAccountId ?: 0L,
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.CATEGORY_MANAGER -> {
              CategoryManagerScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.DEBTS -> {
              DebtScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.CUSTOMIZE_DASHBOARD -> {
              EditDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.COMMISSIONS -> {
              CommissionScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.BUDGETS -> {
              BudgetScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.SUBSCRIPTIONS -> {
              SubscriptionsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.RECORDS -> {
              RecordsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.SETTINGS -> {
              SettingsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD },
                onNavigateToWidgets = { currentDestination = MainDestination.HOME_SCREEN_WIDGETS },
                onNavigateToApkReleases = { currentDestination = MainDestination.APK_RELEASES }
              )
            }
            MainDestination.HOME_SCREEN_WIDGETS -> {
              HomeScreenWidgetsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.RECEIPTS_INBOX -> {
              ReceiptInboxScreen(
                viewModel = dashboardViewModel,
                initialTriggerCamera = openQuickPic,
                onNavigateBack = {
                  quickPicState = false
                  currentDestination = MainDestination.DASHBOARD
                  destinationState = MainDestination.DASHBOARD
                }
              )
            }
            MainDestination.ASSETS -> {
              AssetsScreen(
                viewModel = dashboardViewModel,
                onNavigateBack = { currentDestination = MainDestination.DASHBOARD }
              )
            }
            MainDestination.APK_RELEASES -> {
              ApkReleaseScreen(
                onNavigateBack = { currentDestination = MainDestination.SETTINGS }
              )
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: android.content.Intent?) {
    val quickPic = intent?.getBooleanExtra("QUICK_PIC", false) ?: false
    val dest = when {
      quickPic -> MainDestination.RECEIPTS_INBOX
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "RECEIPTS" -> MainDestination.RECEIPTS_INBOX
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "BUDGETS" -> MainDestination.BUDGETS
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "RECORDS" -> MainDestination.RECORDS
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "DEBTS" -> MainDestination.DEBTS
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "WIDGETS" -> MainDestination.HOME_SCREEN_WIDGETS
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "ASSETS" -> MainDestination.ASSETS
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "RELEASES" -> MainDestination.APK_RELEASES
      intent?.getStringExtra("OPEN_DESTINATION")?.uppercase() == "APK" -> MainDestination.APK_RELEASES
      else -> MainDestination.DASHBOARD
    }
    destinationState = dest
    quickAddState = intent?.getBooleanExtra("QUICK_ADD", false) ?: false
    quickPicState = quickPic
  }
}

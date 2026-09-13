package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.StringsDefinition
import com.example.ui.theme.SaffronLight
import com.example.ui.theme.TealPrimary

enum class AppTab {
    HOME,
    SALES,
    EXPENSES,
    INVENTORY,
    INSIGHTS
}

@Composable
fun BazaarBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    strings: StringsDefinition,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                label = strings.navHome,
                selected = selectedTab == AppTab.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home",
                onClick = { onTabSelected(AppTab.HOME) }
            )
            NavTabItem(
                label = strings.navSales,
                selected = selectedTab == AppTab.SALES,
                selectedIcon = Icons.Filled.ShoppingCart,
                unselectedIcon = Icons.Outlined.ShoppingCart,
                testTag = "nav_sales",
                onClick = { onTabSelected(AppTab.SALES) }
            )
            NavTabItem(
                label = strings.navExpenses,
                selected = selectedTab == AppTab.EXPENSES,
                selectedIcon = Icons.Filled.ReceiptLong,
                unselectedIcon = Icons.Outlined.ReceiptLong,
                testTag = "nav_expenses",
                onClick = { onTabSelected(AppTab.EXPENSES) }
            )
            NavTabItem(
                label = strings.navInventory,
                selected = selectedTab == AppTab.INVENTORY,
                selectedIcon = Icons.Filled.Inventory2,
                unselectedIcon = Icons.Outlined.Inventory2,
                testTag = "nav_inventory",
                onClick = { onTabSelected(AppTab.INVENTORY) }
            )
            NavTabItem(
                label = strings.navInsights,
                selected = selectedTab == AppTab.INSIGHTS,
                selectedIcon = Icons.Filled.AutoAwesome,
                unselectedIcon = Icons.Outlined.AutoAwesome,
                testTag = "nav_insights",
                onClick = { onTabSelected(AppTab.INSIGHTS) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val activeColor = TealPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) activeColor.copy(alpha = 0.12f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else inactiveColor,
            maxLines = 1
        )
    }
}

@Composable
fun FloatingVoiceMicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .testTag("floating_mic_button"),
        shape = CircleShape,
        containerColor = TealPrimary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Voice Entry",
            modifier = Modifier.size(28.dp)
        )
    }
}

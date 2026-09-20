package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.model.DashboardCardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class HeroBackgroundStyle(
    val title: String,
    val description: String
) {
    SOLID("Theme Solid", "Material theme default surface"),
    GRADIENT("Ocean Indigo", "Deep indigo and royal blue"),
    SUNSET_GLOW("Sunset Glow", "Vibrant crimson, magenta & orange"),
    EMERALD_AURORA("Emerald Aurora", "Lush emerald & mint hues"),
    MIDNIGHT_NEBULA("Midnight Nebula", "Cosmic purple, violet & dark navy"),
    GOLDEN_LUXE("Golden Luxe", "Warm amber, gold & bronze tones");

    companion object {
        fun fromString(value: String?): HeroBackgroundStyle {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SOLID
        }
    }
}

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_DASHBOARD_CARDS = stringSetPreferencesKey("active_dashboard_cards")
        val DASHBOARD_CARD_ORDER = stringPreferencesKey("dashboard_card_order")
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val HERO_BACKGROUND_STYLE = stringPreferencesKey("hero_background_style")
        val INCLUDE_ASSETS_IN_HERO = booleanPreferencesKey("include_assets_in_hero")
        val HIDE_MONEY = booleanPreferencesKey("hide_money")
    }

    val hideMoney: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HIDE_MONEY] ?: false
        }

    suspend fun setHideMoney(hide: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_MONEY] = hide
        }
    }

    val includeAssetsInHero: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INCLUDE_ASSETS_IN_HERO] ?: true
        }

    suspend fun setIncludeAssetsInHero(include: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_ASSETS_IN_HERO] = include
        }
    }

    val heroBackgroundStyle: Flow<HeroBackgroundStyle> = context.userDataStore.data
        .map { preferences ->
            HeroBackgroundStyle.fromString(preferences[PreferencesKeys.HERO_BACKGROUND_STYLE])
        }

    suspend fun setHeroBackgroundStyle(style: HeroBackgroundStyle) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.HERO_BACKGROUND_STYLE] = style.name
        }
    }

    val baseCurrency: Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BASE_CURRENCY] ?: "PHP"
        }

    suspend fun setBaseCurrency(currency: String) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_CURRENCY] = currency.uppercase().trim()
        }
    }

    val activeDashboardCards: Flow<Set<DashboardCardType>> = context.userDataStore.data
        .map { preferences ->
            val savedNames = preferences[PreferencesKeys.ACTIVE_DASHBOARD_CARDS]
            if (savedNames == null) {
                DashboardCardType.DEFAULT_CARDS
            } else {
                val parsed = savedNames.mapNotNull { DashboardCardType.fromString(it) }.toSet()
                if (parsed.isEmpty()) DashboardCardType.DEFAULT_CARDS else parsed
            }
        }

    val dashboardCardOrder: Flow<List<DashboardCardType>> = context.userDataStore.data
        .map { preferences ->
            val rawOrder = preferences[PreferencesKeys.DASHBOARD_CARD_ORDER]
            if (rawOrder.isNullOrBlank()) {
                DashboardCardType.entries
            } else {
                val parsed = rawOrder.split(",").mapNotNull { DashboardCardType.fromString(it.trim()) }
                val result = parsed.toMutableList()
                DashboardCardType.entries.forEach { entry ->
                    if (!result.contains(entry)) {
                        result.add(entry)
                    }
                }
                result
            }
        }

    suspend fun setCardOrder(order: List<DashboardCardType>) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.DASHBOARD_CARD_ORDER] = order.joinToString(",") { it.name }
        }
    }

    suspend fun setActiveCards(cards: Set<DashboardCardType>) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_DASHBOARD_CARDS] = cards.map { it.name }.toSet()
        }
    }

    suspend fun toggleCard(card: DashboardCardType, isEnabled: Boolean) {
        context.userDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.ACTIVE_DASHBOARD_CARDS]?.toMutableSet()
                ?: DashboardCardType.DEFAULT_CARDS.map { it.name }.toMutableSet()

            if (isEnabled) {
                current.add(card.name)
            } else {
                current.remove(card.name)
            }
            preferences[PreferencesKeys.ACTIVE_DASHBOARD_CARDS] = current
        }
    }
}

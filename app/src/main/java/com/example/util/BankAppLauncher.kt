package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

data class BankAppInfo(
    val displayName: String,
    val packageName: String,
    val alternatePackages: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
) {
    val allPackages: List<String>
        get() = listOf(packageName) + alternatePackages
}

object BankAppLauncher {

    val POPULAR_BANK_APPS: List<BankAppInfo> = listOf(
        BankAppInfo(
            displayName = "MariBank",
            packageName = "com.maribank.ph",
            alternatePackages = listOf("com.maribank.sg", "com.maribank.id", "ph.com.maribank"),
            keywords = listOf("maribank", "mari bank", "mari", "shopee maribank")
        ),
        BankAppInfo(
            displayName = "GCash",
            packageName = "com.globe.gcash.android",
            alternatePackages = listOf("ph.com.gcash", "com.gcash"),
            keywords = listOf("gcash", "g-cash", "globe cash", "g cash")
        ),
        BankAppInfo(
            displayName = "Maya",
            packageName = "com.voyagerinnovation.paymaya",
            alternatePackages = listOf("com.paymaya", "ph.com.paymaya"),
            keywords = listOf("maya", "paymaya", "pay maya")
        ),
        BankAppInfo(
            displayName = "SeaBank",
            packageName = "ph.com.seabank.mobile",
            alternatePackages = listOf("com.seabank.ph", "id.co.seabank"),
            keywords = listOf("seabank", "sea bank", "sea")
        ),
        BankAppInfo(
            displayName = "BDO Pay / Online",
            packageName = "com.bdo.pay",
            alternatePackages = listOf("com.bdo.online", "ph.com.bdo"),
            keywords = listOf("bdo", "bdo pay", "bdo digital", "bdo unibank", "bdo online")
        ),
        BankAppInfo(
            displayName = "BPI",
            packageName = "com.bpi.ng.app",
            alternatePackages = listOf("com.bpi.mobile", "ph.com.bpi"),
            keywords = listOf("bpi", "bpi app", "bank of the philippine islands", "bpi express")
        ),
        BankAppInfo(
            displayName = "UnionBank",
            packageName = "com.unionbankph.online",
            alternatePackages = listOf("ph.com.unionbank"),
            keywords = listOf("unionbank", "union bank", "ub")
        ),
        BankAppInfo(
            displayName = "GoTyme Bank",
            packageName = "ph.gotyme.app",
            alternatePackages = listOf("com.gotyme"),
            keywords = listOf("gotyme", "go tyme", "gotyme bank")
        ),
        BankAppInfo(
            displayName = "Tonik",
            packageName = "com.tonik.mobile",
            alternatePackages = emptyList(),
            keywords = listOf("tonik", "tonik bank")
        ),
        BankAppInfo(
            displayName = "ShopeePay",
            packageName = "com.shopee.ph",
            alternatePackages = listOf("com.shopee.id", "com.shopee.my", "com.shopee.sg"),
            keywords = listOf("shopeepay", "shopee", "shopee pay")
        ),
        BankAppInfo(
            displayName = "GrabPay",
            packageName = "com.grabtaxi.passenger",
            alternatePackages = emptyList(),
            keywords = listOf("grabpay", "grab", "grab pay")
        ),
        BankAppInfo(
            displayName = "PayPal",
            packageName = "com.paypal.android.p2pmobile",
            alternatePackages = emptyList(),
            keywords = listOf("paypal", "pay pal")
        ),
        BankAppInfo(
            displayName = "Revolut",
            packageName = "com.revolut.revolut",
            alternatePackages = emptyList(),
            keywords = listOf("revolut")
        ),
        BankAppInfo(
            displayName = "Wise",
            packageName = "com.transferwise.android",
            alternatePackages = emptyList(),
            keywords = listOf("wise", "transferwise")
        ),
        BankAppInfo(
            displayName = "Cash App",
            packageName = "com.squareup.cash",
            alternatePackages = emptyList(),
            keywords = listOf("cash app", "cashapp", "square cash")
        ),
        BankAppInfo(
            displayName = "Venmo",
            packageName = "com.venmo",
            alternatePackages = emptyList(),
            keywords = listOf("venmo")
        ),
        BankAppInfo(
            displayName = "CIMB Bank PH",
            packageName = "com.cimb.cimbph",
            alternatePackages = listOf("com.cimb.cimbclicks"),
            keywords = listOf("cimb", "cimb bank", "octo")
        ),
        BankAppInfo(
            displayName = "Komo by EastWest",
            packageName = "ph.com.eastwestbank.komo",
            alternatePackages = listOf("com.eastwest.mobile"),
            keywords = listOf("komo", "eastwest komo", "eastwest")
        ),
        BankAppInfo(
            displayName = "RCBC Pulz",
            packageName = "com.rcbc.pulz",
            alternatePackages = listOf("com.diskartech"),
            keywords = listOf("rcbc", "rcbc pulz", "diskartech")
        ),
        BankAppInfo(
            displayName = "Metrobank",
            packageName = "com.metrobank.mobile",
            alternatePackages = listOf("ph.com.metrobank.mbapp"),
            keywords = listOf("metrobank", "metro bank")
        )
    )

    /**
     * Resolves matching BankAppInfo based on an account or payment mode name.
     */
    fun findMatchingBankApp(accountName: String, explicitPackage: String? = null): BankAppInfo? {
        if (!explicitPackage.isNullOrBlank()) {
            val byPkg = POPULAR_BANK_APPS.find { app ->
                app.allPackages.any { it.equals(explicitPackage, ignoreCase = true) }
            }
            if (byPkg != null) return byPkg
            return BankAppInfo(accountName, explicitPackage)
        }

        val cleaned = accountName.lowercase().trim()
        return POPULAR_BANK_APPS.find { appInfo ->
            appInfo.keywords.any { kw -> cleaned.contains(kw) }
        }
    }

    /**
     * Checks if a package is currently installed on this Android device.
     */
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens the banking app directly if installed, or directs the user to Google Play Store / chooser.
     */
    fun launchBankApp(context: Context, accountName: String, explicitPackage: String? = null) {
        val targetApp = findMatchingBankApp(accountName, explicitPackage)
        val packageCandidates = buildList {
            if (!explicitPackage.isNullOrBlank()) add(explicitPackage.trim())
            if (targetApp != null) {
                addAll(targetApp.allPackages)
            }
        }.distinct()

        val label = targetApp?.displayName ?: accountName

        // 1. Try launching through standard getLaunchIntentForPackage for each candidate
        for (pkg in packageCandidates) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(launchIntent)
                    Toast.makeText(context, "Opening $label...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                // Try next
            }
        }

        // 2. Try querying main launcher activity explicitly for each candidate
        for (pkg in packageCandidates) {
            try {
                val queryIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    `package` = pkg
                }
                val resolveInfos = context.packageManager.queryIntentActivities(queryIntent, 0)
                if (resolveInfos.isNotEmpty()) {
                    val activityInfo = resolveInfos[0].activityInfo
                    val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setClassName(activityInfo.packageName, activityInfo.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                    context.startActivity(explicitIntent)
                    Toast.makeText(context, "Opening $label...", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                // Try next
            }
        }

        // 3. If target package candidate exists but not installed, prompt to open Play Store
        val primaryPackage = packageCandidates.firstOrNull()
        if (!primaryPackage.isNullOrBlank()) {
            Toast.makeText(context, "$label app not installed. Opening Google Play...", Toast.LENGTH_SHORT).show()
            openPlayStore(context, primaryPackage)
        } else {
            // No package known, search on Play Store for the account name
            Toast.makeText(context, "Searching Google Play for $accountName...", Toast.LENGTH_SHORT).show()
            searchPlayStore(context, accountName)
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    private fun searchPlayStore(context: Context, query: String) {
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$query")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=$query&c=apps")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}

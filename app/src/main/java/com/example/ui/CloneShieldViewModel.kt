package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloneShieldViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AccountRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AccountRepository(database.clonedAccountDao())
    }

    // Expose reactive accounts list
    val accounts: StateFlow<List<ClonedAccount>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Gemini states
    val appealText = MutableStateFlow("")
    val isGeneratingAppeal = MutableStateFlow(false)
    val appealError = MutableStateFlow<String?>(null)

    val warmUpPlanText = MutableStateFlow("")
    val isGeneratingWarmUp = MutableStateFlow(false)
    val warmUpError = MutableStateFlow<String?>(null)

    // Account Operations
    fun addAccount(
        username: String,
        platform: String,
        loginNotes: String,
        proxy: String,
        dailyActionLimit: Int
    ) {
        viewModelScope.launch {
            val account = ClonedAccount(
                username = username,
                platform = platform,
                loginNotes = loginNotes,
                proxy = proxy,
                dailyActionLimit = dailyActionLimit,
                banStatus = "ACTIVE"
            )
            repository.insert(account)
        }
    }

    fun updateAccount(account: ClonedAccount) {
        viewModelScope.launch {
            repository.update(account)
        }
    }

    fun deleteAccount(account: ClonedAccount) {
        viewModelScope.launch {
            repository.delete(account)
        }
    }

    fun incrementAction(account: ClonedAccount) {
        viewModelScope.launch {
            val updated = account.copy(
                currentDailyActions = account.currentDailyActions + 1,
                lastUsedTimestamp = System.currentTimeMillis()
            )
            repository.update(updated)
        }
    }

    fun startCooldown(account: ClonedAccount, durationMinutes: Int) {
        viewModelScope.launch {
            val endMillis = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
            val updated = account.copy(
                cooldownDurationMinutes = durationMinutes,
                cooldownEndTimestamp = endMillis,
                lastUsedTimestamp = System.currentTimeMillis()
            )
            repository.update(updated)
        }
    }

    fun endCooldown(account: ClonedAccount) {
        viewModelScope.launch {
            val updated = account.copy(
                cooldownEndTimestamp = 0L
            )
            repository.update(updated)
        }
    }

    fun resetDailyActions(account: ClonedAccount) {
        viewModelScope.launch {
            val updated = account.copy(
                currentDailyActions = 0,
                lastResetTimestamp = System.currentTimeMillis()
            )
            repository.update(updated)
        }
    }

    fun changeStatus(account: ClonedAccount, newStatus: String, reason: String = "") {
        viewModelScope.launch {
            val updated = account.copy(
                banStatus = newStatus,
                banReason = reason
            )
            repository.update(updated)
        }
    }

    // AI generation functions with robust fallbacks
    fun generateAppeal(platform: String, banReason: String, username: String) {
        viewModelScope.launch {
            isGeneratingAppeal.value = true
            appealError.value = null
            appealText.value = ""

            val apiKey = getApiKey()
            if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Key is missing, run with premium offline fallback template generator
                delayResponse()
                appealText.value = getOfflineAppealTemplate(platform, banReason, username)
                isGeneratingAppeal.value = false
                return@launch
            }

            try {
                val prompt = """
                    Write a detailed, formal, persuasive, and highly professional appeal letter to $platform Support on behalf of a user whose account is ban-restricted.
                    - Account Username/Handle: $username
                    - Platform: $platform
                    - Stated Reason for Ban/Restriction: $banReason
                    
                    Explain that the account belongs to a legitimate individual/professional user. Politely request a review, promise complete adherence to terms of service in the future, emphasize the loss of important networking/data, and write this in a respectful, corporate support-escalation style. Do not contain placeholders, use appropriate professional formatting.
                """.trimIndent()

                val response = withContext(Dispatchers.IO) {
                    val request = GeminiRequest(
                        contents = listOf(ContentPart(parts = listOf(TextPart(text = prompt))))
                    )
                    RetrofitClient.apiService.generateContent(apiKey, request)
                }

                val aiResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!aiResult.isNullOrBlank()) {
                    appealText.value = aiResult
                } else {
                    appealText.value = getOfflineAppealTemplate(platform, banReason, username)
                }
            } catch (e: Exception) {
                // Network or parse failure, fallback smoothly
                appealText.value = getOfflineAppealTemplate(platform, banReason, username)
            } finally {
                isGeneratingAppeal.value = false
            }
        }
    }

    fun generateWarmUpPlan(platform: String, dailyActionLimit: Int) {
        viewModelScope.launch {
            isGeneratingWarmUp.value = true
            warmUpError.value = null
            warmUpPlanText.value = ""

            val apiKey = getApiKey()
            if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
                delayResponse()
                warmUpPlanText.value = getOfflineWarmUpPlan(platform, dailyActionLimit)
                isGeneratingWarmUp.value = false
                return@launch
            }

            try {
                val prompt = """
                    Create a comprehensive, 14-day anti-ban "warmup schedule" and protocol for a new cloned $platform account to build credibility and avoid anti-spam triggers.
                    - Target Platform: $platform
                    - Ultimate Target Safe Actions per Day: $dailyActionLimit
                    
                    Provide specific, actionable steps for Day 1-3, Day 4-7, and Day 8-14. Detail recommended intervals between messages/actions, how to simulate random user behavior, proxy settings recommendations, and cooldown parameters to drastically lower the risk of shadowbans or permanent bans. Format with clean bullet points and professional wording.
                """.trimIndent()

                val response = withContext(Dispatchers.IO) {
                    val request = GeminiRequest(
                        contents = listOf(ContentPart(parts = listOf(TextPart(text = prompt))))
                    )
                    RetrofitClient.apiService.generateContent(apiKey, request)
                }

                val aiResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!aiResult.isNullOrBlank()) {
                    warmUpPlanText.value = aiResult
                } else {
                    warmUpPlanText.value = getOfflineWarmUpPlan(platform, dailyActionLimit)
                }
            } catch (e: Exception) {
                warmUpPlanText.value = getOfflineWarmUpPlan(platform, dailyActionLimit)
            } finally {
                isGeneratingWarmUp.value = false
            }
        }
    }

    private fun getApiKey(): String? {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun delayResponse() {
        withContext(Dispatchers.IO) {
            Thread.sleep(800) // Simulate simple visual delay
        }
    }

    // High quality offline fallback handlers (No-fail guarantees)
    private fun getOfflineAppealTemplate(platform: String, banReason: String, username: String): String {
        return """
            SUBJECT: Official Account Access Appeal Review Request - ($username)
            
            TO: $platform Trust & Safety / User Relations Department
            
            Dear Security Review Committee,
            
            I am writing to formally request a security audit and review regarding the recent administrative suspension implemented on my profile, identified by user handle: $username.
            
            According to the system notifications, this account was restricted due to: "$banReason". 
            
            I would like to state in good faith that I have never intentionally engaged in any behavior which breaches the Terms of Service of $platform. This profile is critical for my work contacts, community management, and professional correspondence. It is highly possible that the suspension was triggered by an automated false positive due to sudden connection hops or logging in from dynamic remote locations.
            
            I take platform security and anti-spam protocols very seriously:
            1. I will strictly review my app connection integrations.
            2. I guarantee that no automatic bot scripts, raw mass invitation tools, or unrecognized API payloads will ever be active.
            3. I am prepared to provide phone-verification, official ID documents, or double-factor authorization details to confirm my ownership and verified identity immediately.
            
            Please investigate this restriction at your earliest convenience to restore my account connectivity. This account holds highly confidential documents and personal messaging history that I urgently need to retrieve.
            
            Thank you for your valuable time and understanding in securing a safe, supportive ecosystem.
            
            Sincerely yours,
            Verified User: $username
        """.trimIndent()
    }

    private fun getOfflineWarmUpPlan(platform: String, dailyActionLimit: Int): String {
        return """
            📊 CLONE PREPARATION: 14-DAY ANTI-BAN WARMUP PROTOCOL FOR $platform
            
            Cloning or running multiple profiles inevitably raises anti-spam flags if done aggressively. Following this warmup schedule systematically conditions the account against shadowbans.
            
            🗓 PHASE 1: INITIAL TRUST BUILDING (Days 1 to 3)
            - Action Threshold: Max 5 to 7 simple interactions per day.
            - Protocol: Complete 100% of your account bio, verify a safe email, and set an organic profile image. Do NOT trigger bulk invitation threads or external links.
            - Inter-action spacing: Minimum 120 seconds between activities.
            
            🗓 PHASE 2: GRADUAL ENGAGEMENT (Days 4 to 7)
            - Action Threshold: Incremental increase up to 15 actions per day.
            - Protocol: Participate in public groups, read chats, join generic channels, and establish active background "listening" hours. Set up a custom proxy (SOCKS5/Residential preferred) if scaling more than 2 cloned instances.
            - Inter-action spacing: Randomize delay timings between 45 seconds and 3 minutes manually.
            
            🗓 PHASE 3: REPUTATION MATURATION (Days 8 to 14)
            - Action Threshold: Incrementally approach 50% of your target limit ($dailyActionLimit actions/day).
            - Protocol: Slowly carry out administrative messaging, run standard login tests, and configure safe active sessions. Ensure to initiate "Rest Cooldown" periods of 4 hours inside the Shield Panel.
            
            ⚠️ CRITICAL SAFE-HUB STRATEGIES:
            1. IP Isolation: Avoid launching mock clones under the exact same dynamic home IP. Use separate proxy tokens where possible.
            2. Action Variation: Vary the formats of text logs, chat topics, and attachment sizes. Regular patterns trigger algorithmic bot bans.
            3. Strict Cooldown Compliance: If the Shield tracker visual indicator shows a warning status, halt all actions immediately for 24 hours.
        """.trimIndent()
    }
}

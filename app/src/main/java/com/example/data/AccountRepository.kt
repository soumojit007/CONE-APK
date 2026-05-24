package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class AccountRepository(private val clonedAccountDao: ClonedAccountDao) {
    
    // Auto-resets daily action counts when fetched if the day has changed
    val allAccounts: Flow<List<ClonedAccount>> = clonedAccountDao.getAllAccounts().map { accounts ->
        accounts.map { account ->
            if (shouldResetActions(account)) {
                val updated = account.copy(
                    currentDailyActions = 0,
                    lastResetTimestamp = System.currentTimeMillis()
                )
                // We run a background update silently if necessary, but returning the corrected entity maintains pure StateFlow sync in UI
                clonedAccountDao.updateAccount(updated)
                updated
            } else {
                account
            }
        }
    }

    suspend fun insert(account: ClonedAccount) {
        clonedAccountDao.insertAccount(
            account.copy(
                lastResetTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun update(account: ClonedAccount) {
        val finalAccount = if (shouldResetActions(account)) {
            account.copy(
                currentDailyActions = 0,
                lastResetTimestamp = System.currentTimeMillis()
            )
        } else {
            account
        }
        clonedAccountDao.updateAccount(finalAccount)
    }

    suspend fun delete(account: ClonedAccount) {
        clonedAccountDao.deleteAccount(account)
    }

    suspend fun getById(id: Int): ClonedAccount? {
        val account = clonedAccountDao.getAccountById(id) ?: return null
        return if (shouldResetActions(account)) {
            val updated = account.copy(
                currentDailyActions = 0,
                lastResetTimestamp = System.currentTimeMillis()
            )
            clonedAccountDao.updateAccount(updated)
            updated
        } else {
            account
        }
    }

    private fun shouldResetActions(account: ClonedAccount): Boolean {
        if (account.lastResetTimestamp == 0L) return true
        val today = Calendar.getInstance()
        val lastReset = Calendar.getInstance().apply { timeInMillis = account.lastResetTimestamp }
        return today.get(Calendar.YEAR) != lastReset.get(Calendar.YEAR) ||
                today.get(Calendar.DAY_OF_YEAR) != lastReset.get(Calendar.DAY_OF_YEAR)
    }
}

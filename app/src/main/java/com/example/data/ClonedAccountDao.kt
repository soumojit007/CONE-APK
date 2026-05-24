package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClonedAccountDao {
    @Query("SELECT * FROM cloned_accounts ORDER BY id DESC")
    fun getAllAccounts(): Flow<List<ClonedAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: ClonedAccount)

    @Update
    suspend fun updateAccount(account: ClonedAccount)

    @Delete
    suspend fun deleteAccount(account: ClonedAccount)

    @Query("SELECT * FROM cloned_accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): ClonedAccount?
}

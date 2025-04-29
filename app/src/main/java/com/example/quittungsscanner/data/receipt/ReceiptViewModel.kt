package com.example.quittungsscanner.data.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quittungsscanner.data.database.UserDao
import com.example.quittungsscanner.data.database.UserEntity
import com.example.quittungsscanner.data.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.sql.Timestamp
import javax.inject.Inject

/*
@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val userDao: UserDao
): ViewModel() {

    fun addReceipt(receipt: Receipt) {
        viewModelScope.launch {
            userDao.insertReceipt()
        }
    }

    fun getReceipt(): Flow<List<UserEntity>> {
        return userDao.flowLoadAllUsers()
    }
}

data class Receipt(
    val timestamp: Timestamp,
    var items:Items
)*/

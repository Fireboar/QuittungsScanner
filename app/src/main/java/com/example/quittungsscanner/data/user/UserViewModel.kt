package com.example.quittungsscanner.data.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// import com.example.quittungsscanner.data.database.UserDao
// com.example.quittungsscanner.data.database.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDataStore: UserDataStore,
   // private val userDao: UserDao
) : ViewModel() {
    val userFlow: StateFlow<User> = userDataStore
        .user
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            User("", -1, false)
        )

    fun updateUser(user: User) {
        viewModelScope.launch {
            userDataStore.setUserName(user.name)
            userDataStore.setUserAge(user.age)
            userDataStore.setUserAuthorized(user.authorized)
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            //val userEntity = UserEntity(name = user.name, age = user.age, authorized = user.authorized)
            //userDao.insertUser(userEntity)
        }
    }

    //fun getUsers(): Flow<List<UserEntity>> {
    //    return userDao.flowLoadAllUsers()
    //}


}


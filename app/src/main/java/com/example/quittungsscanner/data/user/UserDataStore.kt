package com.example.quittungsscanner.data.user

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDataStore @Inject constructor (
    @ApplicationContext private val context: Context
){
    companion object{
        private const val USER_PREFERENCES_NAME = "user_preferences"
        private val Context.dataStore by preferencesDataStore(
            name = USER_PREFERENCES_NAME
        )
    }

    val user: Flow<User> = context.dataStore.data.map{ preferences ->
        val userName = preferences[PreferencesKeys.USER_NAME] ?: ""
        val userAge = preferences[PreferencesKeys.USER_AGE] ?: 0
        val userAuthorized = preferences[PreferencesKeys.USER_AUTHORIZED] ?: false
        User(
            name = userName,
            age = userAge,
            authorized = userAuthorized
        )
    }

    suspend fun setUserName(name:String){
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setUserAge(age: Int){
        context.dataStore.edit {preferences ->
            preferences[PreferencesKeys.USER_AGE] = age
        }
    }

    suspend fun setUserAuthorized(authorized: Boolean){
        context.dataStore.edit {preferences ->
            preferences[PreferencesKeys.USER_AUTHORIZED] = authorized
        }
    }

    // Keys
    private object PreferencesKeys{
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AGE = intPreferencesKey("user_age")
        val USER_AUTHORIZED = booleanPreferencesKey("user_authorized")
    }

}

data class User(
    val name: String,
    val age: Int,
    val authorized: Boolean
)
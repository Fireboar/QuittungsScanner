package com.example.quittungsscanner

import app.cash.turbine.test
import com.example.quittungsscanner.data.database.UserDao
import com.example.quittungsscanner.data.database.UserEntity
import com.example.quittungsscanner.data.user.User
import com.example.quittungsscanner.data.user.UserDataStore
import com.example.quittungsscanner.data.user.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @Test
    fun testUpdateUser() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val userDataStoreMock = mock<UserDataStore>()
        val userDao = mock<UserDao>()
        val user = User("Hans Muster", 35, false)
        val viewModel = UserViewModel(
            userDataStoreMock,
            userDao = userDao
        )

        viewModel.updateUser(user)

        verify(userDataStoreMock).setUserName(user.name)
        verify(userDataStoreMock).setUserAge(user.age)
        verify(userDataStoreMock).setUserAuthorized(user.authorized)

        Dispatchers.resetMain()
    }

    @Test
    fun testAddUsers() = runTest {
        val userDao = mock<UserDao>()
        val userDataStore = mock<UserDataStore>()

        val userViewModel = UserViewModel(userDataStore, userDao)

        val testUser = User("Test User", 25, true)
        userViewModel.addUser(testUser)

        val expectedUserEntity = UserEntity(name = "Test User", age = 25, authorized = true)
        verify(userDao).insertUser(expectedUserEntity)
    }

    @Test
    fun testUserFlow() = runTest {
        val expectedUser = User("Hans Muster", 35, false)
        val firstUser = User("", -1, false)

        val userDataStoreMock = mock<UserDataStore>()
        val userDaoMock = mock<UserDao>()

        whenever(userDataStoreMock.user).thenReturn(
            flowOf(expectedUser)
        )

        val viewModel = UserViewModel(userDataStoreMock, userDaoMock)

        viewModel.userFlow.test {
            var user = awaitItem()
            Assert.assertEquals(user, firstUser)
            val second = awaitItem()
            Assert.assertEquals(expectedUser, second)
        }

    }

}
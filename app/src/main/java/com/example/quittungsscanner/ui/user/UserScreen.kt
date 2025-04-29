package com.example.quittungsscanner.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quittungsscanner.data.user.User
import com.example.quittungsscanner.data.user.UserViewModel


@Composable
fun UserScreen() {
    val userViewModel = hiltViewModel<UserViewModel>()
    val user by userViewModel.userFlow.collectAsState()

    val userList by userViewModel.getUsers().collectAsState(initial = emptyList())
    /*LaunchedEffect(userList.size) {
        onUpdateBadgeCount(userList.size)
    }*/

    Column {
        UserInputs(
            userViewModel = userViewModel,
            user = user,
            onUpdateUser = { updatedUser ->
                userViewModel.updateUser(updatedUser)
            }
        )

        Column {
            Text("${userList.size}")
        }


        LazyColumn {
            items(userList){ user->
                Text("${user.name}, Age ${user.age}, enabled ${user.authorized}")
            }
        }
    }
}

@Composable
private fun UserInputs(
    userViewModel: UserViewModel,
    user: User,
    onUpdateUser: (User) -> Unit
) {
    var name by remember(user.name) { mutableStateOf(user.name) }
    var age by remember(user.age) { mutableStateOf(user.age.toString()) }
    var authorized by remember(user.authorized) { mutableStateOf(user.authorized) }

    Column {
        TextField(
            modifier = Modifier
                .padding(top = 16.dp),
            value = name,
            onValueChange = { newName ->
                name = newName
            },
            label = { Text("Name") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            modifier = Modifier
                .padding(top = 16.dp),
            value = age,
            onValueChange = { newAge ->
                age = newAge
            },
            label = { Text("Alter") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Switch(
            checked = authorized,
            onCheckedChange = { isChecked ->
                authorized = isChecked
            }
        )

        Button(
            onClick = {
                val userAge = age.toIntOrNull() ?: 0
                val updatedUser = User(name, userAge, authorized)
                userViewModel.addUser(updatedUser)
                userViewModel.updateUser(updatedUser)
                onUpdateUser(updatedUser)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Speichern")
        }
    }
}

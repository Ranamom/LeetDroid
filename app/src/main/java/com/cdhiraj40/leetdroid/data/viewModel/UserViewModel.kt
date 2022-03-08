package com.cdhiraj40.leetdroid.data.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope

import com.cdhiraj40.leetdroid.data.db.UserDatabase
import com.cdhiraj40.leetdroid.data.entitiy.User
import com.cdhiraj40.leetdroid.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    val getUser: LiveData<User>

    init {
        val userDB = UserDatabase.getInstance(application).userDao()
        userRepository = UserRepository(userDB)
        getUser = userRepository.user(1)
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.insertUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUser(user)
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteUser(id)
        }
    }
}

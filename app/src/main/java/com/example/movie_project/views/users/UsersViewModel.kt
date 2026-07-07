package com.example.movie_project.views.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.data.repository.UsersRepository
import com.example.movie_project.models.domain.UsersModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class UsersUiState(
    val users: List<UsersModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UsersRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<UsersUiState> = repository.observeUsers()
        .map { allUsers ->
            val currentUid = authRepository.currentUserId
            UsersUiState(
                users = allUsers.filter { it.uid != currentUid },
                isLoading = false
            )
        }
        .catch { e -> emit(UsersUiState(isLoading = false, error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UsersUiState()
        )
}

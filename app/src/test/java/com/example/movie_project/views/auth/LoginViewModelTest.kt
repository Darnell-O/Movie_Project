package com.example.movie_project.views.auth

import com.example.movie_project.MainDispatcherRule
import com.example.movie_project.data.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        authRepository = mock()
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun login_emptyFields_setsErrorAndDoesNotCallRepository() = runTest {
        viewModel.login()

        assertEquals("Email and password are required", viewModel.uiState.value.error)
        verify(authRepository, never()).login(any(), any())
    }

    @Test
    fun login_success_navigatesToMain() = runTest {
        whenever(authRepository.login("a@b.com", "pw")).thenReturn(Result.success(Unit))
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("pw")

        viewModel.login()

        val state = viewModel.uiState.value
        assertTrue(state.navigateToMain)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun login_failure_surfacesError() = runTest {
        whenever(authRepository.login("a@b.com", "pw"))
            .thenReturn(Result.failure(Exception("Invalid credentials")))
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("pw")

        viewModel.login()

        val state = viewModel.uiState.value
        assertFalse(state.navigateToMain)
        assertFalse(state.isLoading)
        assertEquals("Invalid credentials", state.error)
    }
}
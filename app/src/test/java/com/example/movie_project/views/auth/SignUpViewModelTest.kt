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

class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setup() {
        authRepository = mock()
        viewModel = SignUpViewModel(authRepository)
    }

    @Test
    fun signUp_emptyFields_setsErrorAndDoesNotCallRepository() = runTest {
        viewModel.signUp()

        assertEquals("All fields are required", viewModel.uiState.value.error)
        verify(authRepository, never()).signUp(any(), any())
    }

    @Test
    fun signUp_mismatchedPasswords_setsErrorAndDoesNotCallRepository() = runTest {
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("pw1")
        viewModel.onConfirmPasswordChange("pw2")

        viewModel.signUp()

        assertEquals("Passwords do not match", viewModel.uiState.value.error)
        verify(authRepository, never()).signUp(any(), any())
    }

    @Test
    fun signUp_success_navigatesToMain() = runTest {
        whenever(authRepository.signUp("a@b.com", "pw")).thenReturn(Result.success(Unit))
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("pw")
        viewModel.onConfirmPasswordChange("pw")

        viewModel.signUp()

        val state = viewModel.uiState.value
        assertTrue(state.navigateToMain)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun signUp_failure_surfacesError() = runTest {
        whenever(authRepository.signUp("a@b.com", "pw"))
            .thenReturn(Result.failure(Exception("Email already in use")))
        viewModel.onEmailChange("a@b.com")
        viewModel.onPasswordChange("pw")
        viewModel.onConfirmPasswordChange("pw")

        viewModel.signUp()

        val state = viewModel.uiState.value
        assertFalse(state.navigateToMain)
        assertFalse(state.isLoading)
        assertEquals("Email already in use", state.error)
    }
}
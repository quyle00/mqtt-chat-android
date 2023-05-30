package com.quyt.mqttchat.domain.usecase.contact

import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class GetListContactUseCaseTest {

    @Mock
    private lateinit var mockUserRepository: UserRepository

    private lateinit var getListContactUseCase: GetListContactUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        getListContactUseCase = GetListContactUseCase(mockUserRepository)
    }

    @Test
    fun `get list contacts success should return list of users`() = runBlocking {
        val userList = listOf(
            User(),
            User(),
            User()
        )

        `when`(mockUserRepository.getListContacts()).thenReturn(Result.Success(userList))

        val result = getListContactUseCase()

        assertEquals(Result.Success(userList), result)
    }

    @Test
    fun `get list contacts failure should return error`() = runBlocking {
        val errorMessage = "Failed to get contacts"
        val exception = Exception(errorMessage)

        `when`(mockUserRepository.getListContacts()).thenReturn(Result.Error(exception))

        val result = getListContactUseCase()

        assertEquals(Result.Error(exception), result)
    }
}

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.lang.Exception
import com.quyt.mqttchat.domain.model.Result
import com.quyt.mqttchat.domain.model.User
import com.quyt.mqttchat.domain.repository.AccessRepository
import com.quyt.mqttchat.domain.usecase.access.LoginUseCase

class LoginUseCaseTest {

    @Mock
    private lateinit var mockAccessRepository: AccessRepository

    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        loginUseCase = LoginUseCase(mockAccessRepository)
    }

    @Test
    fun `login success should return user`() = runBlocking {
        val username = "john"
        val password = "password"
        val user = User().apply {
            this.id = "1"
            this.fullname = "John Doe"
            this.username = "john"
            this.avatar = "https://www.google.com"
        }

        `when`(mockAccessRepository.login(username, password)).thenReturn(Result.Success(user))

        val result = loginUseCase(username, password)

        assertEquals(Result.Success(user), result)
    }

    @Test
    fun `login failure should return error`() = runBlocking {
        val username = "john"
        val password = "password"
        val errorMessage = "Invalid credentials"
        val exception = Exception(errorMessage)

        `when`(mockAccessRepository.login(username, password)).thenReturn(Result.Error(exception))

        val result = loginUseCase(username, password)

        assertEquals(Result.Error(exception), result)
    }
}
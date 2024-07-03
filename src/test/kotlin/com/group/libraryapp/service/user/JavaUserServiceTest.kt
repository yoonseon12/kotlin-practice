package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class JavaUserServiceTest @Autowired constructor (
    private val userRepository: UserRepository,
    private val userService: UserService,
) {
    @AfterEach
    fun init() {
        userRepository.deleteAll()
    }

    @Test
    fun saveUserTest() {
        // given
        val request = UserCreateRequest("이윤선", null)

        // when
        userService.saveUser(request)

        // then
        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        assertThat(users[0].name).isEqualTo("이윤선")
        assertThat(users[0].age).isNull()
    }

    @Test
    fun getUserTest() {
        // given
        userRepository.saveAll(
            listOf(
                User("이",1),
                User("윤",12),
                User("선",null)
            ))

        // when
        val users = userService.getUsers()

        // then
        assertThat(users).hasSize(3)
        assertThat(users).extracting("name").containsExactlyInAnyOrder("이","윤","선")
        assertThat(users).extracting("age").containsExactlyInAnyOrder(1, 12, null)
    }

    @Test
    fun updateUserNameTest() {
        // given
        val saveUser = userRepository.save(User("이윤선", 2))
        val request = UserUpdateRequest(saveUser.id!!, "수정함")

        // when
        userService.updateUserName(request)

        // then
        val findUser = userRepository.findById(saveUser.id).orElseThrow{IllegalArgumentException()}
        assertThat(findUser.name).isEqualTo(request.name)
    }

    @Test
    fun deleteUserTest() {
        // given
        val saveUser = userRepository.save(User("이윤선", 2))

        // when
        userService.deleteUser(saveUser.name)

        // then
        val findUser = userRepository.findByName(saveUser.name)
        assertThat(findUser).isNull()
    }
}
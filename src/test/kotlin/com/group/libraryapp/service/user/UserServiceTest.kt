package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistory
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.userlonehistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor (
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {
    @AfterEach
    fun init() {
        println("클린 시작")
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

    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다.")
    fun getUserLoanHistoriesTest1() {
        // given
        userRepository.save(User("A", null))

        // when
        val userLoanHistories = userService.getUserLoanHistories()

        // then
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].name).isEqualTo("A")
        assertThat(userLoanHistories[0].books).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 많은 유저의 응답이 정상 동작한다.")
    fun getUserLoanHistoriesTest2() {
        // given
        val saveUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(saveUser, "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(saveUser, "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(saveUser, "책3", UserLoanStatus.RETURNED),
        ))


        // when
        val userLoanHistories = userService.getUserLoanHistories()

        // then
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].name).isEqualTo("A")
        assertThat(userLoanHistories[0].books).extracting("name")
            .containsExactlyInAnyOrder("책1","책2","책3")
        assertThat(userLoanHistories[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)
    }

    @Test
    @DisplayName("Test1 + Test2")
    fun getUserLoanHistoriesTest3() {
        // given
        val saveUsers = userRepository.saveAll(listOf(
            User("A", null),
            User("B", null),
        ))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(saveUsers[0], "책1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(saveUsers[0], "책2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(saveUsers[0], "책3", UserLoanStatus.RETURNED),
        ))

        // when
        val userLoanHistories = userService.getUserLoanHistories()

        // then
        assertThat(userLoanHistories).hasSize(2)

        val userAResult = userLoanHistories.first { it.name == "A" }
        assertThat(userAResult.books).hasSize(3)
        assertThat(userAResult.books).extracting("name")
            .containsExactlyInAnyOrder("책1","책2","책3")
        assertThat(userAResult.books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)

        val userBResult = userLoanHistories.first { it.name == "B" }
        assertThat(userBResult.books).isEmpty()
    }


}
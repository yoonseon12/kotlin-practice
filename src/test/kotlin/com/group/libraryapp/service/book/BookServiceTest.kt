package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistory
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookRepository: BookRepository,
    private val bookService: BookService,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
){

    @AfterEach
    fun cleanBook() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @DisplayName("책 등록이 정상 동작한다.")
    fun saveBookTest() {
        // given
        val request = BookRequest("엘리스")

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books.size).isEqualTo(1)
        assertThat(books[0].name).isEqualTo(request.name)
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다.")
    fun loanBookTest() {
        // given
        bookRepository.save(Book("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        val request = BookLoanRequest("이윤선", "엘리스")

        // when
        bookService.loanBook(request)

        // then
        val userLoanBooks = userLoanHistoryRepository.findAll()
        assertThat(userLoanBooks.size).isEqualTo(1)
        assertThat(userLoanBooks[0].bookName).isEqualTo(request.bookName)
        assertThat(userLoanBooks[0].user.id).isEqualTo(savedUser.id)
        assertThat(userLoanBooks[0].isReturn).isEqualTo(false)
    }
    
    @Test
    @DisplayName("대출하고있는 책을 대출시 대출에 실패한다.")
    fun loanBookExceptionTest() {
        // given
        bookRepository.save(Book("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                savedUser,
                "엘리스",
                false
            )
        )
        val request = BookLoanRequest("이윤선", "엘리스")

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }
        assertThat(exception.message).isEqualTo("진작 대출되어 있는 책입니다.")
    }

    @Test
    @DisplayName("책 반납이 정상 동작한다.")
    fun returnBookTest() {
        // given
        val savedBook = bookRepository.save(Book("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        userLoanHistoryRepository.save(
            UserLoanHistory(
                savedUser,
                savedBook.name,
                false
            )
        )
        val request = BookReturnRequest(savedUser.name, savedBook.name)

        // when
        bookService.returnBook(request)

        // then
        val userLoanHistories = userLoanHistoryRepository.findAll()
        assertThat(userLoanHistories[0].isReturn).isTrue()

    }
}
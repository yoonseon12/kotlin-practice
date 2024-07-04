package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistory
import com.group.libraryapp.domain.user.userlonehistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.userlonehistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
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
        val request = BookRequest("엘리스", BookType.COMPUTER)

        // when
        bookService.saveBook(request)

        // then
        val books = bookRepository.findAll()
        assertThat(books.size).isEqualTo(1)
        assertThat(books[0].name).isEqualTo(request.name)
        assertThat(books[0].type).isEqualTo(request.type)
    }

    @Test
    @DisplayName("책 대출이 정상 동작한다.")
    fun loanBookTest() {
        // given
        bookRepository.save(Book.fixture("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        val request = BookLoanRequest("이윤선", "엘리스")

        // when
        bookService.loanBook(request)

        // then
        val userLoanBooks = userLoanHistoryRepository.findAll()
        assertThat(userLoanBooks.size).isEqualTo(1)
        assertThat(userLoanBooks[0].bookName).isEqualTo(request.bookName)
        assertThat(userLoanBooks[0].user.id).isEqualTo(savedUser.id)
        assertThat(userLoanBooks[0].status).isEqualTo(UserLoanStatus.LOANED)
    }
    
    @Test
    @DisplayName("대출하고있는 책을 대출시 대출에 실패한다.")
    fun loanBookExceptionTest() {
        // given
        bookRepository.save(Book.fixture("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        userLoanHistoryRepository.save(
            UserLoanHistory.fixture(
                savedUser,
                "엘리스",
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
        val savedBook = bookRepository.save(Book.fixture("엘리스"))
        val savedUser = userRepository.save(User("이윤선", 20))
        userLoanHistoryRepository.save(
            UserLoanHistory.fixture(
                savedUser,
                savedBook.name,
            )
        )
        val request = BookReturnRequest(savedUser.name, savedBook.name)

        // when
        bookService.returnBook(request)

        // then
        val userLoanHistories = userLoanHistoryRepository.findAll()
        assertThat(userLoanHistories[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }
    
    @Test
    @DisplayName("책 대여 권수를 정상 확인한다.")
    fun countLoanedBookTest() {
        // given
        val savedUser = userRepository.save(User("이윤선", 20))
        val savedBook = bookRepository.save(Book.fixture("책1"))
        userLoanHistoryRepository.saveAll(
            listOf(
                UserLoanHistory.fixture(savedUser, "책1"),
                UserLoanHistory.fixture(savedUser, "책2", UserLoanStatus.RETURNED),
                UserLoanHistory.fixture(savedUser, "책3", UserLoanStatus.RETURNED))
        )

        // when
        val countLoanedBook = bookService.countLoanedBook()

        // then
        assertThat(countLoanedBook).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 책 권수를 정상 확인한다.")
    fun getBookStatisticsTest() {
        // given
        bookRepository.saveAll(listOf(
            Book.fixture("책1",BookType.COMPUTER),
            Book.fixture("책2",BookType.COMPUTER),
            Book.fixture("책3",BookType.ECONOMY),
            Book.fixture("책4",BookType.SCIENCE),
        ))

        // when
        val bookStatistics = bookService.getBookStatistics()

        // then
        assertThat(bookStatistics.size).isEqualTo(3)
        assertCount(bookStatistics, BookType.COMPUTER, 2L)
        assertCount(bookStatistics, BookType.ECONOMY, 1L)
        assertCount(bookStatistics, BookType.SCIENCE, 1L)
    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        val computersDto = results.first { result -> result.type == type }
        assertThat(computersDto.count).isEqualTo(count)
    }
}
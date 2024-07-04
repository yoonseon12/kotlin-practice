package com.group.libraryapp.repository.book

import com.group.libraryapp.domain.book.QBook.*
import com.group.libraryapp.dto.book.response.BookStatResponse
import com.group.libraryapp.dto.book.response.QBookStatResponse
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Component

@Component
class BookQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {

    fun getStatus(): List<BookStatResponse> {
        return queryFactory
            .select(
                QBookStatResponse(
                book.type,
                book.id.count()
            ))
            .from(book)
            .groupBy(book.type)
            .fetch()
    }

}
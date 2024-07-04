package com.group.libraryapp.domain.book

import javax.persistence.*

@Entity
class Book(
    var name: String,

    @Enumerated(EnumType.STRING)
    val type: BookType,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) {

    init {
        require(name.isNotBlank()) { "이름은 비어 있을 수 없습니다" }
//        if (name.isBlank()) {
//            throw IllegalArgumentException("이름은 비어 있을 수 없습니다")
//        }
    }

    companion object {
        fun fixture(
            name: String = "",
            type: BookType = BookType.COMPUTER,
            id: Long? = null,
        ): Book {
            return Book(
                name = name,
                type = type,
                id = id,
            )
        }
    }

}
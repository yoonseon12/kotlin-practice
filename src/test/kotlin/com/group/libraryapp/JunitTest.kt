package com.group.libraryapp

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class JunitTest {

    @BeforeEach
    fun init() {
        println("Before init")
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            println("BeforeAll")

        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            println("afterAll")

        }
    }

    @Test
    @DisplayName("")
    fun test1() {
        println("test1")
    }

    @Test
    @DisplayName("")
    fun test2() {
        println("test1")
    }
}
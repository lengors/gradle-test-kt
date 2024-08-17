package io.github.lengors.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LibraryTest {
    @Test
    fun someLibraryMethodReturnsTrue() {
        val classUnderTest = Library()
        Assertions.assertEquals("Hello, I'm 20 years old!", classUnderTest.test(20u))
    }
}

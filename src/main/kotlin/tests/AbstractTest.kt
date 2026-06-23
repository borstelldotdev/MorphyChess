package main.tests

interface AbstractTestCase {
    fun runTestCase()
}

interface AbstractTest {
    fun generateTestCases(): List<AbstractTestCase>
}
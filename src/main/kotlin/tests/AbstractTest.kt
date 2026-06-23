package main.tests

interface AbstractTestCase {
    fun runTestCase(): Boolean
}

interface AbstractTest {
    fun generateTestCases(): List<AbstractTestCase>
}
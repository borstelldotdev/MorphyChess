package main.tests

fun runTests() {
    val testClasses = listOf<AbstractTest>(
        PerftTest()
    )

    for (testClass in testClasses) {
        val tests = testClass.generateTestCases()
        println("Executing ${testClass::class.simpleName} (${tests.size} test cases)")

        for (testCase in tests) {
            testCase.runTestCase()
        }
    }

    println("Finished executing tests")
}
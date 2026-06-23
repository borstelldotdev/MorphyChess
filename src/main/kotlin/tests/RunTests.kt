package main.tests

// Yes, this is a stupid way to do testing
// However, I really can't get normal tests to compile properly
// So I'm resorting to this to still be able to test my code

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
package main.tests

// Yes, this is a stupid way to do testing
// However, I really can't get normal tests to compile properly
// So I'm resorting to this to still be able to test my code

fun runTests() {
    val testClasses = listOf<AbstractTest>(
        PerftTest()
    )

    var totalTestCases = 0
    var passedTestCases = 0

    for (testClass in testClasses) {
        val tests = testClass.generateTestCases()
        println("Executing ${testClass::class.simpleName} (${tests.size} test cases)")

        for (testCase in tests) {
            totalTestCases ++
            val result = testCase.runTestCase()
            if (result) passedTestCases ++
        }
    }

    println("Finished executing tests")
    println("$passedTestCases passed, ${totalTestCases - passedTestCases} failed")
}
// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// KJS_WITH_FULL_RUNTIME
// WITH_RUNTIME
import kotlin.test.*

fun box(): String {
    assertFailsWith<IllegalArgumentException> {
        val intProgression = 1 until 8
        for (i in intProgression step -1) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        val longProgression = 1L until 8L
        for (i in longProgression step -1L) {
        }
    }

    assertFailsWith<IllegalArgumentException> {
        val charProgression = 'a' until 'h'
        for (i in charProgression step -1) {
        }
    }

    return "OK"
}
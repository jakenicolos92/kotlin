// PROBLEM: none

fun test(i: Int) {
    <caret>if (i == 1) foo() else {
    }
}

fun foo() {}
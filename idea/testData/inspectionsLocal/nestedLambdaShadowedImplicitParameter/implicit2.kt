// FIX: Add explicit parameter name to outer lambda

fun foo(f: (String) -> Unit) {}
fun bar(f: (Int) -> Unit) {}

fun test() {
    foo {
        val s: String = it
        bar {
            val i: Int = it<caret>
        }
    }
}
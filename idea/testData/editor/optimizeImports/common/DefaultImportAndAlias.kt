// WITH_MESSAGE: "Rearranged imports"
import pack1.a
import pack1.a as a1
import kotlin.collections.List
import kotlin.collections.List as List1
import kotlin.run as run1
import kotlin.run

fun foo() {
    run {}
    run1 {}
    List<String>(1) {}
    List1<String>(1) {}
    a()
    a1()
}
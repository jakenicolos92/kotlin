package test

expect fun foo(x: Int)

fun test() {
    foo(1)
    foo(x = 1)
}
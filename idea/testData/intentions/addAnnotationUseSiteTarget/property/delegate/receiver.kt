// CHOOSE_USE_SITE_TARGET: receiver
// WITH_RUNTIME
// IS_APPLICABLE: false

annotation class A

class Property {
    @A<caret>
    val foo: String by lazy { "" }
}
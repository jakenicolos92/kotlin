// CHOOSE_USE_SITE_TARGET: set
// IS_APPLICABLE: false

@Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER)
annotation class C

class Extension

val @C<caret> Extension.bar: String
    get() = ""
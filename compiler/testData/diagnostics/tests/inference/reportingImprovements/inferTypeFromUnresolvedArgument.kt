// !WITH_NEW_INFERENCE
// !DIAGNOSTICS: -UNUSED_PARAMETER

fun <K> id2(x: K, s: String): K = x
fun <K> ret(s: String): K = TODO()

fun test() {
    id2(<!UNRESOLVED_REFERENCE!>unresolved<!>, "foo")
    id2(<!UNRESOLVED_REFERENCE!>unresolved<!>, <!CONSTANT_EXPECTED_TYPE_MISMATCH!>42<!>)

    <!NI;NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, OI;TYPE_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>ret<!>("foo")
    <!OI;TYPE_INFERENCE_PARAMETER_CONSTRAINT_ERROR!>ret<!>(<!CONSTANT_EXPECTED_TYPE_MISMATCH!>42<!>)
}
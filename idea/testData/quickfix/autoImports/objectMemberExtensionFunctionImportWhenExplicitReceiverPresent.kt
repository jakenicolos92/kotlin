// "Import" "true"
package p

class T
class NotT
class NotTT

object TopLevelObject1 {
    fun T.foobar() {}
}

object TopLevelObject2 {
    fun NotT.foobar() {} // other type, should not be imported
}

object TopLevelObject3 {
    fun foobar() {} // not an extension, should not be imported
}

object TopLevelObject4 {
    fun NotTT.foobar() {} // NotTT present as receiver, but explicit receiver shuts him down
}

fun NotTT.usage(t: T) {
    t.<caret>foobar()
}
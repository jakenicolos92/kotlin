// KJS_WITH_FULL_RUNTIME
// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_RUNTIME


val MinUI = UInt.MIN_VALUE
val MinUB = UByte.MIN_VALUE
val MinUS = UShort.MIN_VALUE
val MinUL = ULong.MIN_VALUE

fun box(): String {
    val list1 = ArrayList<UInt>()
    val range1 = (MinUI + 2u) downTo MinUI step 1
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<UInt>(MinUI + 2u, MinUI + 1u, MinUI)) {
        return "Wrong elements for (MinUI + 2u) downTo MinUI step 1: $list1"
    }

    val list2 = ArrayList<UInt>()
    val range2 = (MinUB + 2u).toUByte() downTo MinUB step 1
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<UInt>((MinUB + 2u).toUInt(), (MinUB + 1u).toUInt(), MinUB.toUInt())) {
        return "Wrong elements for (MinUB + 2u).toUByte() downTo MinUB step 1: $list2"
    }

    val list3 = ArrayList<UInt>()
    val range3 = (MinUS + 2u).toUShort() downTo MinUS step 1
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<UInt>((MinUS + 2u).toUInt(), (MinUS + 1u).toUInt(), MinUS.toUInt())) {
        return "Wrong elements for (MinUS + 2u).toUShort() downTo MinUS step 1: $list3"
    }

    val list4 = ArrayList<ULong>()
    val range4 = (MinUL + 2u) downTo MinUL step 1
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<ULong>((MinUL + 2u), (MinUL + 1u), MinUL)) {
        return "Wrong elements for (MinUL + 2u) downTo MinUL step 1: $list4"
    }

    return "OK"
}

fun test() {
    foo@ for (n in 1..10) {
        if (n == 5) continue@foo
        if (n > 8) break@/*rename*/foo
    }
}
// IGNORE_BACKEND_FIR: JVM_IR
// FILE: A.java

public class A {
    public final String field = "OK";
}

// FILE: 1.kt

fun box() = (A::field).get(A())

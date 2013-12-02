package foo


class A(val text: String)

fun test(f: A.() -> String): String {
    val a = A("O")
    return a.f()
}

fun box(): String {
    return test {
        text + "K"
    }
}
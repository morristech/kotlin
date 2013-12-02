package foo.bar

class A(val text: String)

class B(val a : A)

fun B.invoke(): String {
    return this.a.text
}

val A.b : B
    get() {
        return B(this)
    }

fun A.bb() = b

fun box(): String {
    val b = A("O").bb()
    val o = b()
    val k = A("K").b()
    return o + k
}
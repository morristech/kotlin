// !DIAGNOSTICS: -UNUSED_PARAMETER

inline fun fooPlus(plus: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    p + p
}

inline fun fooPlusNoInline(noinline plus: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>p<!> + <!USAGE_IS_NOT_INLINABLE!>p<!>
}

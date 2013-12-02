// !DIAGNOSTICS: -UNUSED_PARAMETER

inline fun fooContains(contains: Any.(p: Function0<Int>) -> Boolean, p: () -> Int) {
    p in p
}

inline fun fooContainsNoInline(noinline contains: Any.(p: Function0<Int>) -> Boolean, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>p<!> in <!USAGE_IS_NOT_INLINABLE!>p<!>
}
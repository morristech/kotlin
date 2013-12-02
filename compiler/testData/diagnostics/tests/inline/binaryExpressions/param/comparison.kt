// !DIAGNOSTICS: -UNUSED_PARAMETER

inline fun fooCompareTo(compareTo: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    p < p
}

inline fun fooCompareToNoInline(noinline compareTo: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>p<!> < <!USAGE_IS_NOT_INLINABLE!>p<!>
}

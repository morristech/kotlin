// !DIAGNOSTICS: -UNUSED_PARAMETER

inline fun fooRange(rangeTo: Any.(p: Function0<Int>) -> Range<Int>, p: () -> Int) {
    p..p
}

inline fun fooRangeNoInline(noinline rangeTo: Any.(p: Function0<Int>) -> Range<Int>, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>p<!>..<!USAGE_IS_NOT_INLINABLE!>p<!>
}


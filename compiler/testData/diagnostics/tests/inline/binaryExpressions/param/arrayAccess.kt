// !DIAGNOSTICS: -UNUSED_PARAMETER

inline fun fooArray(get: Any.(Int) -> Int, set: Any.(Int, Int) -> Unit, p: () -> Int) {
    p[1]
    p[1] = 2
}

inline fun fooArrayNoInline(noinline get: Any.(Int) -> Int, noinline set: Any.(Int, Int) -> Unit, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>p<!>[1]
    <!USAGE_IS_NOT_INLINABLE!>p<!>[1] = 2
}


inline fun Function0<Int>.Array(get: Any.(Int) -> Int, set: Any.(Int, Int) -> Unit, p: () -> Int) {
    this[1]
    this[1] = 2
}

inline fun Function0<Int>.ArrayNoInline(noinline get: Any.(Int) -> Int, noinline set: Any.(Int, Int) -> Unit, p: () -> Int) {
    <!USAGE_IS_NOT_INLINABLE!>this<!>[1]
    <!USAGE_IS_NOT_INLINABLE!>this<!>[1] = 2
}

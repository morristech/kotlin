// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

inline fun fooComponent(component1: Any.(p: Function0<Int>) -> Int, component2: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    var (s, t) = p
}

inline fun fooComponentNoInline(noinline component1: Any.(p: Function0<Int>) -> Int, noinline component2: Any.(p: Function0<Int>) -> Int, p: () -> Int) {
    var (s, t) = <!USAGE_IS_NOT_INLINABLE!>p<!>
}


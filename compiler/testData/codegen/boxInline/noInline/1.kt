inline fun calc(s: (Int) -> Int, noinline p: (Int) -> Int) : Int {
    p.hashCode()
    return s(11) + p(11)
}
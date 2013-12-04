fun test1(): Int {
    return calc( {(l : Int) -> 2*l},  {(l : Int) -> 4*l})
}


fun box(): String {
    if (test1() != 66) return "test1: ${test1()}"

    return "OK"
}
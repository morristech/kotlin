package demo
open class Test() {
open fun getInteger(i : Int) : Int {
return i
}
open fun test() : Unit {
val i = getInteger(10)!!
}
}
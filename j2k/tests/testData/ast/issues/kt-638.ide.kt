public open class Identifier<T>(_myName : T, _myHasDollar : Boolean) {
private val myName : T
private var myHasDollar : Boolean = false
private var myNullable : Boolean = true
public open fun getName() : T {
return myName
}
{
myName = _myName
myHasDollar = _myHasDollar
}
class object {
public open fun init<T>(name : T) : Identifier<T> {
val __ = Identifier(name, false)
return __
}
public open fun init<T>(name : T, isNullable : Boolean) : Identifier<T> {
val __ = Identifier(name, false)
__.myNullable = isNullable
return __
}
public open fun init<T>(name : T, hasDollar : Boolean, isNullable : Boolean) : Identifier<T> {
val __ = Identifier(name, hasDollar)
__.myNullable = isNullable
return __
}
}
}
public open class User() {
class object {
public open fun main(args : Array<String>) {
val i1 = Identifier.init<String>("name", false, true)
val i2 = Identifier.init<String>("name", false)
val i3 = Identifier.init<String>("name")
}
}
}
fun main(args : Array<String>) = User.main(args as Array<String?>?)
package test
class Base() {
public fun hashCode() : Int {
return System.identityHashCode(this)
}
public fun equals(o : Any) : Boolean {
return this.identityEquals(o)
}
public fun toString() : String {
return getJavaClass<Base>.getName() + '@' + Integer.toHexString(hashCode())
}
}
class Child() : Base() {
public override fun hashCode() : Int {
return super.hashCode()
}
public override fun equals(o : Any) : Boolean {
return super.equals(o)
}
public override fun toString() : String {
return super.toString()
}
}
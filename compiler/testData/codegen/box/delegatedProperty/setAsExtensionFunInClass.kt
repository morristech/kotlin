class Delegate {
    var inner = 1
    fun get(t: Any?, p: String): Int = inner
}

class A {
  fun Delegate.set(t: Any?, p: String, i: Int) { inner = i }

  var prop: Int by Delegate()
}

fun box(): String {
  val c = A()
  if(c.prop != 1) return "fail get"
  c.prop = 2
  if (c.prop != 2) return "fail set"
  return "OK"
}
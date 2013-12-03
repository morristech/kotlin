
fun box(): String {
    val result = doWork({11})
    if (result != 11) return "test1: ${result}"

    return "OK"
}
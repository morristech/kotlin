package foo.bar


val Collection<*>.indices : IntRange
    get() = 0..3

public inline fun <T> List<T>.forEachWithIndex(operation : (Int, T) -> Unit) {
    for (index in indices) {
        operation(index, get(index))
    }
}


fun box(): String {
    return "OK"
}

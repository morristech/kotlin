package kotlin


private object _Assertions
private val ASSERTIONS_ENABLED = _Assertions.javaClass.desiredAssertionStatus()

/**
* Throws an [[AssertionError]] with an optional *message* if the *value* is false
* and runtime assertions have been enabled on the JVM using the *-ea* JVM option.
*/
public inline fun assert(value: Boolean, message: Any = "Assertion failed") {
    if (ASSERTIONS_ENABLED) {
        if (!value) {
            throw AssertionError(message)
        }
    }
}

/**
 * Throws an [[AssertionError]] with the specified *lazyMessage* if the *value* is false
 * and runtime assertions have been enabled on the JVM using the *-ea* JVM option.
 */
public inline fun assert(value: Boolean, lazyMessage: () -> String) {
    if (ASSERTIONS_ENABLED) {
        if (!value) {
            val message = lazyMessage()
            throw AssertionError(message)
        }
    }
}
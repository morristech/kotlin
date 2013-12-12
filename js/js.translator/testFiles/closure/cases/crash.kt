/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foo

fun run<T>(f: () -> T) = f()

fun boo(i: Int = 0): Int = if (i == 0) i else boo(i - 1)

val r = "OK"

fun barrr(s: String? = null): String {
    if (s != null) return s

    //bar("OK")
    return run {
        barrr(r)
    }
}

val ok = barrr()

fun box(): String {
    fun baz() = 1
    fun boo(i: Int = 0): Int = if (i == 0) i else boo(i - 1)

    val v = 3
    fun coo(i: Int = 0): Int = if (i == 0) baz() + v else coo(i - 1)

    fun bar(s: String? = null): String {
        if (s != null) return s

        //bar("OK")
        return run {
            bar("OK")
        }
    }

    //val fff: (Int) -> Int = { i -> if (i == 0) i else fff(i) }

    if (ok != "OK") return "notOK"

    return bar()

    //return "OK"
}

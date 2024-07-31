package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class AsyncFunction {

    /**
     * * launch and async are coroutines builder.
     * * Conceptually, async is just like launch.
     * * launch return a job (Job is an interface that finally is a CoroutineContext) but
     * async return a deferred (Deferred is an interface and subType of Job.)
     * * We can not return result from coroutine that created by launch.
     * * deferred.await -----> Suspends the coroutine until the result is available and returns the result.
     */

    fun fun1() {
        runBlocking {
            val first = async {
                printYellow("before delay 2")
                delay(4000)
                printYellow("after delay 2")

                return@async 12
            }

            val second = async {
                printYellow("before delay 1")
                delay(2000)
                printYellow("after delay 1")

                return@async 25
            }

            val result = first.await() + second.await()

            printYellow("3")
            printYellow("result = $result")
        }
    }


    /**
     * * Start modes in coroutine builder:
     *    * CoroutineStart.DEFAULT is default mode
     *    * CoroutineStart.ATOMIC is similar to CoroutineStart.DEFAULT but
     *    the coroutine cannot be cancelled before it starts executing.
     *    * In this mode (CoroutineStart.LAZY) it only starts the coroutine when
     *    its result is required by await, or if its Job's start function is invoked
     *    * CoroutineStart.UNDISPATCHED
     * * We can use this modes for launch and async
     */

    fun fun2() {
        runBlocking {
            val second = async(start = CoroutineStart.LAZY) {
                printYellow("before delay 1")
                delay(2000)
                printYellow("after delay 1")

                return@async 25
            }

            val first = async(start = CoroutineStart.LAZY) {
                printYellow("before delay 2")
                delay(4000)
                printYellow("after delay 2")

                return@async 12
            }

            first.start()
            second.start()

            val result = first.await() + second.await()

            printYellow("3")
            printYellow("result = $result")
        }
    }
}
package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printBlue
import ir.hrka.coroutines.helpers.Log.printRed
import ir.hrka.coroutines.helpers.Log.printWhite
import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield

class CancellationCoroutines {

    /**
     * * Coroutine cancellation is cooperative.
     * * A coroutine code has to cooperate to be cancellable.
     * * All the suspending functions in kotlinx.coroutines are cancellable (delay, yield, whitTimeOut, withContext).
     * * They check for cancellation of coroutine and throw CancellationException when cancelled.
     * * Job is an interface.
     * * job.join : Suspends until the coroutine completes but does not cancel it.
     * * job.cancel : Cancels the coroutine but does not wait for its termination.
     * * job.cancelChildren : Cancels all children coroutines of the job.
     * * job.cancelAndJoin : Cancels the coroutine and then waits for its termination, ensuring that the coroutine has fully terminated before proceeding.
     * * job.start : Start Coroutine in the lazy mode.
     * * job.isActive : A property that returns true if the job is still active (i.e., it has not completed and has not been canceled).
     * * job.isCompleted : A property that returns true if the job has completed for any reason, including normal completion, cancellation, or failure.
     * * job.isCancelled : A property that returns true if the job has been canceled.
     * * job.children : A sequence of the coroutine's children jobs. This can be used to manage and monitor child coroutines.
     * * job.invokeOnCompletion : Registers handler that is **synchronously** invoked once on completion of this job.
     */

    fun fun1() {
        runBlocking {
            val job = launch {
                var passedTime = 0
                repeat(50) { i ->
                    printYellow("before delay $i")
                    delay(500)
                    passedTime += 500
                    printYellow("after delay $i ---> Tha passed time = $passedTime")
                }
            }
            printYellow("before cancellation delay")
            delay(2300)
            job.cancelAndJoin()
            printYellow("after cancellation delay")
        }
    }


    /**
     * While catching Exception is an anti-pattern, this issue may surface in more subtle ways, like when using the runCatching function, which does not rethrow CancellationException.
     */

    fun fun2() {
        runBlocking {
            val job = launch {
                var passedTime = 0
                repeat(50) { i ->
                    try {
                        printYellow("before delay $i")
                        delay(500)
                        passedTime += 500
                        printBlue("after delay $i ---> Tha passed time = $passedTime")
                    } catch (e: CancellationException) {
                        printRed("Catch CancellationException -> ${e.message}")
                    }
                }
            }
            printYellow("before cancellation delay")
            delay(2300)
            job.cancelAndJoin()
            printYellow("after cancellation delay")
        }
    }


    /**
     * * There are two approaches to making computation code cancellable:
     *    * The first one is to periodically invoke a suspending function that checks for cancellation (yield).
     *    * The other one is to explicitly check the cancellation status (if(isActive){} , while(isActive){}).
     */


    fun fun3() {
        runBlocking {
            val job = launch {
                var i = 1
                while (isActive) {
                    printYellow("Coroutine has not been canceled yet (isActive = $isActive):) ${i++}")
                    yield()
                }
            }
            printYellow("before cancellation delay")
            delay(500)
            job.cancelAndJoin()
            printYellow("after cancellation delay")
        }
    }


    /**
     * * You can use finally block to close resources (for example) after coroutine cancellation.
     * * Use withContext(NonCancellable) if you want the code to continue executing even after the program is canceled.
     * * When cancel a coroutine, an exception is thrown and rest of the code dose not execute so
     * withContext(NonCancellable) block dose not execute after cancellation too, so it must be
     * used in the finally block.
     */

    fun fun4() {
        runBlocking {
            val job = launch {
                repeat(50) { i ->
                    try {
                        printYellow("before delay $i")
                        delay(500)
                        printYellow("after delay $i")
                    } finally {
                        withContext(NonCancellable) {
                            printRed("finally before delay")
                            delay(500)
                            printRed("finally after delay")
                        }
                    }
                }
            }
            printWhite("before cancellation delay")
            delay(4300)
            job.cancelAndJoin()
            printWhite("after cancellation delay")
        }
    }


    /**
     * * You can limit the execution of the task using withTimeout(timeMillis) {}.
     * * The TimeoutCancellationException that is thrown on timeout by withTimeout is a subclass of CancellationException.
     * * Like coroutineScope{} withTimeout{} suspends coroutine until end of execution its children coroutine.
     * * If timeout occurs do not execute rest coroutines after withTimeout block.
     */

    fun fun5() {
        runBlocking {
            withTimeout(5000) {
                repeat(50) { i ->
                    printYellow("before delay -> $i")
                    delay(1_000)
                    printYellow("after delay -> $i")
                }
            }

            printYellow("after withTimeout block")
        }
    }


    /**
     * * withTimeoutOrNull function that is similar to withTimeout but returns null on timeout instead of throwing an exception.
     * * Like coroutineScope{} withTimeoutOrNull{} suspends coroutine until end of execution inner coroutine.
     */

    fun fun6() {
        runBlocking {
            val result = withTimeoutOrNull(5000) {
                repeat(10) { i ->
                    printYellow("before delay -> $i")
                    delay(1_000)
                    printYellow("after delay -> $i")
                }

                return@withTimeoutOrNull "Task successfully done."
            }

            launch {
                printYellow("run another coroutine")
            }

            printYellow("result = $result")
        }
    }


    /**
     * * Asynchronous timeout and resources.
     * * The timeout event in withTimeout is asynchronous with respect to the code running in its block and may happen at any time, even right before the return from inside of the timeout block.
     */

    fun fun7() {
        runBlocking {
            repeat(20) {
                val resource = withTimeoutOrNull(60) {
                    delay(59)
                    return@withTimeoutOrNull Resource()
                }

                resource?.close()
            }

            printYellow("Number of resources = ${Resource.count}")
        }
    }

    fun fun8() {
        runBlocking {
            repeat(20) {
                var resource: Resource? = null

                try {
                    withTimeoutOrNull(60) {
                        delay(62)
                        resource = Resource()
                    }
                } finally {
                    resource?.close()
                }
            }

            printYellow("Number of open resources = ${Resource.count}")
        }
    }

    private class Resource {

        companion object {
            var count = 0
        }

        init {
            count++
        }

        fun close() = count--
    }
}


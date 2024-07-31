package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RunBlocking {

    /**
     * * Coroutines = cooperative routines (cooperative functions).
     * * Coroutines can be thought of as light-weight threads (Coroutines are less resource-intensive than JVM threads).
     * * a coroutine is not bound to any particular thread. It may suspend its execution in one thread and resume in another one.
     * * runBlocking is also a coroutine builder that bridges the non-coroutine world of a regular fun and
     * the code with coroutines inside of runBlocking curly braces.
     * * The name of runBlocking means that the thread that runs it gets blocked for the duration of the call,
     * until all the coroutines inside runBlocking complete their execution.
     */

    fun fun1() {
        val job = Job()
        val coroutineName = CoroutineName("runBlocking")
        val threads = Dispatchers.IO
        val coroutineContext = threads + job + coroutineName

        runBlocking(coroutineContext) {
            launch {
                delay(2_000)
                printYellow("3")
            }
            launch {
                printYellow("1")
                delay(4_000)
                printYellow("4")
            }
            delay(1_000)
            printYellow("2")
        }
        printYellow("5")
    }
}
package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CoroutineScopeFunction {

    /**
     * * coroutineScope function creates a coroutine scope and does not complete until all launched children complete.
     * * Coroutines follow a principle of structured concurrency which means that
     * new coroutines can only be launched in a specific CoroutineScope which delimits the lifetime of the coroutine.
     * * runBlocking and coroutineScope builders may look similar because they both wait for their body and all its children to complete.
     * * The main difference is that the runBlocking method blocks the current thread for waiting, while
     * coroutineScope just suspends, releasing the underlying thread for other usages (suspend coroutine).
     * * runBlocking is a regular function and coroutineScope is a suspending function.
     * * If any coroutine within the scope fails, the scope itself fails, and all coroutines within it are cancelled.
     */

    fun fun1() {
        runBlocking {
            privateFun1()
            printYellow("6")
        }
        printYellow("7")
    }


    /**
     * * If fun2 is called, the program will block because the parent coroutine is suspended,
     * so runBlocking will block the main thread because one of the child coroutines has not yet executed.
     */

    fun fun2() {
        runBlocking {
            privateFun2()
            printYellow("7")
        }
        printYellow("1")
    }


    private suspend fun privateFun1() {
        coroutineScope {
            launch {
                delay(2_000)
                printYellow("2")
            }
            launch {
                delay(3_000)
                printYellow("3")
            }
            delay(1_000)
            printYellow("1")
        }

        coroutineScope {
            launch {
                delay(500)
                printYellow("4")
            }
        }

        printYellow("5")
    }

    private suspend fun privateFun2() {
        coroutineScope {
            launch {
                delay(2_000)
                printYellow("3")
            }
            launch {
                delay(3_000)
                printYellow("4")
            }
            delay(1_000)
            printYellow("2")
        }

        coroutineScope {
//            If you want execute this coroutine call start or join on its job
            val job = launch(start = CoroutineStart.LAZY) {
                delay(500)
                printYellow("5")
            }
//            job.join()
        }

        printYellow("6")
    }
}

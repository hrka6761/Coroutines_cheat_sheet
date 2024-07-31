package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printBlue
import ir.hrka.coroutines.helpers.Log.printRed
import ir.hrka.coroutines.helpers.Log.printWhite
import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException


/**
 * * Uncaught exceptions are exceptions that occur during the execution of a program but
 * are not handled by any try-catch blocks or other exception handling mechanisms.
 * * Coroutine builders come in two flavors:
 *    * propagating exceptions automatically (launch) that treat exceptions as uncaught exceptions and
 *    if the exceptions do not handled, the application crashes (When these builders are used to create a root coroutine).
 *    * exposing exceptions to users (async and produce) that are relying on the user to consume the final
 *    exception for example via await or receive (When these builders are used to create a root coroutine).
 */


@Suppress("UNREACHABLE_CODE")
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class ExceptionHandling {

    fun fun1() {
        val job = GlobalScope.launch {
            printYellow("before exception")

            // If you don't use the try-catch block, the app will crash
            try {
                throw Exception("Launch coroutine throw exception !!!!")
            } catch (e: Exception) {
                printRed(e.message.toString())
            }
            printYellow("after exception")
        }

        job.invokeOnCompletion {
            printYellow("End of execution")
        }
    }

    fun fun2() {
        runBlocking {
            val differed = GlobalScope.async {
                printYellow("before exception")
                delay(2_000)
                throw Exception("Async coroutine throw exception !!!!")
                printYellow("after exception")
            }

            // If you don't use the try-catch block, the app will crash
            try {
                delay(1_000)
                printYellow("before await")
                differed.await()
            } catch (e: Exception) {
                printRed(e.message.toString())
            }

            differed.invokeOnCompletion {
                printYellow("End of execution")
            }
        }
    }

    fun fun3() {
        val job = GlobalScope.launch {

            // If you don't use the try-catch block, the app will crash
            try {
                printYellow("before receive")
                createProducer().receive()
            } catch (e: Exception) {
                printRed(e.message.toString())
            }
        }

        job.invokeOnCompletion {
            printYellow("End of execution")
        }
    }


    /**
     * * CoroutineExceptionHandler context element on a root coroutine can be used as a generic catch block
     * for this root coroutine and all its children where custom exception handling may take place.
     * * CoroutineExceptionHandler is invoked only on uncaught exceptions.
     * * CoroutineExceptionHandler implementation is not used for child coroutines.
     * * By using coroutineExceptionHandler, if exception is thrown from coroutines, app wont crash
     * but parent scope will get cancelled.
     */

    fun fun4() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            printRed("exceptionHandler ----> ${throwable.message}")
        }

        val job = Job()
        val coroutineContext = job + exceptionHandler
        val scope = CoroutineScope(coroutineContext)

        scope.launch {
            printYellow("Start coroutine 1")
            delay(1_000)
            throw Exception("coroutine 1 throw exception !!!!")
            printYellow("Complete coroutine 1")
        }

        scope.launch {
            printBlue("Start coroutine 2")
            delay(2_000)
            printBlue("Complete coroutine 2")
        }

        job.invokeOnCompletion {
            printYellow("end of execution")
        }
    }


    /**
     * * Async builder always catches all exceptions and represents them in the resulting Deferred object,
     * so its CoroutineExceptionHandler has no effect either.
     */

    fun fun5() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            printRed("exceptionHandler ----> ${throwable.message}")
        }

        runBlocking {
            val differed = GlobalScope.async(exceptionHandler) {
                printYellow("before exception")
                throw Exception("Test exception by launch !!!!")
                printYellow("after exception")
            }

            // CoroutineExceptionHandler has no effect
            // If you don't use the try-catch block, the app will crash
            try {
                differed.await()
            } catch (e: Exception) {
                printRed(e.message.toString())
            }

            differed.invokeOnCompletion {
                printYellow("end of execution")
            }
        }
    }


    /**
     * * CancellationException is ignored by the coroutines' machinery.
     * * If a coroutine encounters an exception other than CancellationException, it cancels its parent with that exception.
     * * When a coroutine is cancelled using Job.cancel, it terminates, but it does not cancel its parent.
     * * If one of the child coroutine is cancelled, it does not affect other child coroutines or parent coroutine.
     */

    fun fun6() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            printRed("exceptionHandler ----> ${throwable.message}")
        }

        runBlocking {
            val job = Job()
            val coroutineContext = job + exceptionHandler + Dispatchers.Default
            val scope = CoroutineScope(coroutineContext)

            job.invokeOnCompletion {
                printYellow("end of execution")
            }

            printYellow("Start execution")

            val childJob1 = scope.launch {
                printBlue("Start coroutine 1")
                delay(2_000)
                printBlue("Complete coroutine 1")
            }

            val childJob2 = scope.launch {
                printWhite("Start coroutine 2")
                delay(3_000)
                printWhite("Complete coroutine 2")
            }

            val childJob3 = scope.launch {
                printYellow("Start coroutine 3")
                delay(1_000)
                printYellow("Cancel parent coroutine by coroutine 3")
                childJob1.cancelAndJoin()
                printYellow("Complete coroutine 3")
            }
        }
    }


    /**
     * * When multiple children of a coroutine fail with an exception, the general rule is "the first exception wins",
     * so the first exception gets handled. All additional exceptions that happen after the first one
     * are attached to the first exception as suppressed ones.
     */

    fun fun7() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            printRed("exceptionHandler ----> handled exception = ${throwable.message}, suppressed exception = ${throwable.suppressed.contentToString()}")
        }

        val job = Job()
        val coroutineContext = job + exceptionHandler + Dispatchers.Main
        val scope = CoroutineScope(coroutineContext)

        scope.launch {
            printYellow("Start coroutine 1")
            throw Exception("Coroutine 1 throw exception !!!!")
            printYellow("Complete coroutine 1")
        }

        scope.launch {
            printYellow("Start coroutine 2")
            throw Exception("Coroutine 2 throw exception !!!!")
            printYellow("Complete coroutine 2")
        }

        job.invokeOnCompletion {
            printYellow("End of execution")
        }
    }


    /**
     * * cancellation is a bidirectional relationship propagating through the whole hierarchy of coroutines.
     * * Bidirectional cancellation ensures that the entire hierarchy of coroutines is aware of the cancellation event,
     * allowing each coroutine to handle it appropriately.
     * * SupervisorJob is similar to a regular Job with the only exception that cancellation is propagated only downwards.
     * * Cancellation and failures propagate only downward from parent to child.
     */

    fun fun8() {
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            printRed("exceptionHandler ----> handled exception = ${throwable.message}")
        }

        val job = SupervisorJob()
        val coroutineContext = job + exceptionHandler + Dispatchers.Main
        val scope = CoroutineScope(coroutineContext)

        printYellow("Parent coroutine started")

        scope.launch {
            printYellow("Child 1 coroutine started")
            delay(1_000L)
            throw Exception()
            printYellow("Child 1 coroutine completed")
        }

        scope.launch {
            printYellow("Child 2 coroutine started")
            delay(2_000L)
            printYellow("Child 2 coroutine completed")
        }

        scope.launch {
            printYellow("Child 3 coroutine started")
            delay(3_000L)
            printYellow("Child 3 coroutine completed")
        }
    }


    private fun createProducer(): ReceiveChannel<Int> {
        return GlobalScope.produce {
            printYellow("before exception")
            throw Exception("Producer coroutine throw exception !!!!")
            printYellow("after exception")
        }
    }
}

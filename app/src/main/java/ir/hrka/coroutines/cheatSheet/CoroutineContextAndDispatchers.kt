package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CoroutineContextAndDispatchers {

    /**
     * * A coroutine context is indeed a collection of various elements that
     * control the behavior of coroutines (commonly dispatcher and job).
     * * Dispatcher determines the thread or thread pool where the coroutine will be executed.
     * * Job represents the lifecycle of the coroutine (cancel it, join it, start it, get status).
     * * Types of dispatchers:
     *    * Dispatchers.Main (Run coroutine on main thread)
     *    * Dispatchers.IO (Run coroutine on pool of IO thread)
     *    * Dispatchers.Default (Run coroutine on pool of IO thread that optimized for CPU intensive task)
     *    * Dispatchers.Unconfined (start running coroutine on the caller thread but only until the first suspension point)
     *    * newSingleThreadContext creates a thread for the coroutine to run.
     * * We can use explicitly job (Create a common job instance) or implicitly job (returned from coroutine builder).
     * * When launch is used without parameters, it inherits the context from the CoroutineScope it is being launched from.
     * * The unconfined dispatcher is appropriate for coroutines which neither consume CPU time nor
     * update any shared data (like UI) confined to a specific thread.
     * * We can use the + operator for multiple elements for a coroutine context.
     * * Default dispatchers depending on the context in which the coroutine is launched:
     *    * The default dispatcher for runBlocking is confined to the calling thread.
     *    * In GlobalScope uses Dispatchers.Default.
     *    * CoroutineScope without Dispatcher: inherits the dispatcher from the context in which the scope was created.
     *    If no dispatcher is specified in the parent context, it falls back to Dispatchers.Default.
     */

    fun fun1() {
        val job = Job()
        val thread = Dispatchers.Unconfined
        val scopeCoroutineName = CoroutineName("scope coroutine")
        val builderCoroutineName = CoroutineName("builder coroutine")
        val coroutineContext = thread + job + scopeCoroutineName
        val scope = CoroutineScope(coroutineContext)

        scope.launch {
            printYellow("scopeCoroutineName = ${this.coroutineContext[CoroutineName]?.name}")
            printYellow("before delay thread")
            delay(2_000)
            launch(builderCoroutineName) {
                printYellow("before delay thread")
            }
        }
    }


    /**
     * * When a coroutine is launched in the CoroutineScope of another coroutine,
     * it inherits its context via CoroutineScope.coroutineContext and
     * the Job of the new coroutine becomes a child of the parent coroutine's job.
     * When the parent coroutine is cancelled, all its children are recursively cancelled, too.
     */


    fun fun2() {
        runBlocking {
            val parentJob = launch {
                launch(Job()) {
                    printYellow("before delay child coroutine 1")
                    delay(3000)
                    printYellow("after delay child coroutine 1")
                }

                launch {
                    printYellow("before delay child coroutine 2")
                    delay(2000)
                    printYellow("after delay child coroutine 2")
                }
            }

            printYellow("before cancel")
            delay(500)
            parentJob.cancel()
            printYellow("after cancel")
        }
    }
}
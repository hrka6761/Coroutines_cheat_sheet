package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printBlue
import ir.hrka.coroutines.helpers.Log.printWhite
import ir.hrka.coroutines.helpers.Log.printRed
import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.system.measureTimeMillis

class AsynchronousFlow {

    /**
     * * We can use a Sequence<Int> type for synchronously computed values.
     * * We can return multiple asynchronously computed values by kotlin flows.
     * * This computation blocks the main thread that is running the code.
     * * To represent the stream of values that are being computed asynchronously, we can use a Flow<Int>.
     * * Like sequence the code inside a flow builder does not run until the flow is collected.
     */

    fun fun1() {
        printYellow("Start to receive data ...")
        privateFun1().forEach {
            printYellow(it.toString())
        }
        printYellow("End receiving data.")
    }

    fun fun2() {
        printYellow("Start to receive data ...")
        privateFun2().forEach {
            printYellow(it.toString())
        }
        printYellow("End receiving data.")
    }

    fun fun3() {
        runBlocking {
            launch {
                printYellow("Start to receive data ...")
                privateFun3().collect {
                    printYellow("Collect $it")
                    delay(5_000)
                    printYellow("Processed $it")
                }
                printYellow("End receiving data.")
            }

            launch {
                printYellow("Start Another coroutine")
                delay(2_000)
                printYellow("End Another coroutine")
            }
        }

        printYellow("End of computation")
    }


    /**
     * * Cancel Collection
     */

    fun fun4() {
        runBlocking {
            withTimeoutOrNull(3_000) {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    privateFun3()
                        .collect {
                            printYellow("Collect $it")
                            delay(5_00)
                            printYellow("Processed $it")
                        }
                }
                printYellow("End receiving data on $time milli seconds.")
            }

            launch {
                printYellow("Start Another coroutine")
                delay(2_000)
                printYellow("End Another coroutine")
            }
        }

        printYellow("End of computation")
    }


    /**
     * * We can create flow from list by asFlow.
     */

    fun fun5() {
        val list = 1..30
        runBlocking {
            list.asFlow()
                .filter {
                    it > 15
                }.map {
                    "Square of $it is :  ${it * it}"
                }.take(5)
                .onEach { delay(1000) }
                .onCompletion { printBlue("end of computation") }
                .collect {
                    printYellow(it)
                }
        }
    }


    /**
     * * By default, code in the flow { ... } builder (meaning that all operations on the flow (emission,
     * transformation, and collection) runs in the context that is provided by a collector of the corresponding flow.
     * * Code in the flow { ... } builder has to honor the context preservation property and
     * is not allowed to emit from a different context.
     * * The flowOn function that shall be used to change the context of the flow emission.
     */

    fun fun6() {
        runBlocking {
            launch(CoroutineName("Collector")) {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    privateFun4()
                        .collect {
                            printYellow("Collect $it")
                            delay(5_000)
                            printYellow("Processed $it")
                        }
                }
                printYellow("End receiving data on $time milli seconds.")
            }
        }
    }


    /**
     * * When you use a flow without buffering, the emission and collection of items occur sequentially.
     * * We can use a buffer operator on a flow to run emitting code of the simple flow concurrently
     * with collecting code, as opposed to running them sequentially.
     */

    fun fun7() {
        runBlocking {
            launch {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    privateFun3()
                        .buffer(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
                        .collect {
                            printYellow("Collect $it")
                            delay(5_000)
                            printYellow("Processed $it")
                        }
                }
                printYellow("End receiving data on $time milli seconds.")
            }
        }
    }


    /**
     * * Conflation is one way to speed up processing when both the emitter and collector are slow.
     * * When a flow represents partial results of the operation or operation status updates, it may not be necessary to process each value, but instead, only most recent ones.
     * * The conflate operator can be used to skip intermediate values when a collector is too slow to process them
     */

    fun fun8() {
        runBlocking {
            launch {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    privateFun3()
                        .conflate()
                        .collect {
                            printYellow("Collect $it")
                            delay(3_000)
                            printYellow("Processed $it")
                        }
                }
                printYellow("End receiving data on $time milli seconds.")
            }
        }
    }


    /**
     * * The other way is to cancel a slow collector and restart it every time a new value is emitted.
     */

    fun fun9() {
        runBlocking {
            launch {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    privateFun3()
                        .collectLatest {
                            printYellow("Collect $it")
                            delay(3_000)
                            printYellow("Processed $it")
                        }
                }
                printYellow("End receiving data on $time milli seconds.")
            }
        }
    }


    /**
     * * The 'zip' operator combines two flows by pairing corresponding elements.
     * * It waits for both flows to emit before producing a result.
     * * Use 'zip' when you need to pair elements from each flow together.
     */

    fun fun10() {
        runBlocking {
            printYellow("Start to receive data ...")
            val time = measureTimeMillis {
                privateFun3()
                    .zip(privateFun5()) { a, b ->
                        printYellow("Collect $a, $b")
                        delay(3_000)
                        "$a -> $b"
                    }.collect { result ->
                        printRed("Processed and result is = $result")
                    }
            }
            printYellow("End receiving data on $time milli seconds.")
        }
    }


    /**
     * * The 'combine' operator combines two flows by pairing latest elements.
     * * It emits a new result whenever any of the flows emit, using the latest values from all flows.
     * * Use 'combine' when you want to react to changes from any of the flows and work with the most recent values.
     */

    fun fun11() {
        runBlocking {
            printYellow("Start to receive data ...")
            val time = measureTimeMillis {
                privateFun3()
                    .combine(privateFun5()) { a, b ->
                        printYellow("Collect $a, $b")
                        delay(3_000)
                        "$a -> $b"
                    }.collect { result ->
                        printRed("Processed and result is = $result")
                    }
            }
            printYellow("End receiving data on $time milli seconds.")
        }
    }


    /**
     * * flattening flows allows you to transform elements emitted by an upstream flow into another flow.
     * * flatMapConcat: It processes each emitted element by transforming it into a new flow, and concatenates these flows one after another.
     * * flatMapLatest: It is an operator that cancels the previous flow whenever a new element is emitted from the upstream flow.
     * * flatMapMerge:
     */

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fun12() {
        runBlocking {
            privateFun3()
                .flatMapConcat {
                    privateFun5()
                }
                .collect {
                    printYellow("Collect $it")
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fun13() {
        runBlocking {
            privateFun3()
                .flatMapLatest {
                    privateFun5()
                }.collect {
                    printYellow("Collect $it")
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fun14() {
        runBlocking {
            privateFun3()
                .flatMapMerge {
                    privateFun5()
                }
                .collect {
                    printYellow("Collect $it")
                }
        }
    }


    /**
     * * Flow collection can complete with an exception when an emitter or code inside the operators throw an exception.
     * * Throwing exception by using Check() function in the collection
     * * Throwing exception by using Check() function in the emission
     * * We can catch exception by try_catch blocks or by catch function
     */

    fun fun15() {
        runBlocking {
            launch {
                printYellow("Start to receive data ...")
                val time = measureTimeMillis {
                    try {
                        privateFun5()
                            .collect {
                                printYellow("Collect $it")
                                delay(3_000)
                                printYellow("Processed $it")
                                check(
                                    value = it != "three",
                                    lazyMessage = {
                                        printRed("check entire ...")
                                        "three were seen !!!"
                                    })
                            }
                    } catch (e: IllegalStateException) {
                        printYellow("An exception occurred. message = ${e.message}")
                    }
                }
                printYellow("End receiving data on $time milli seconds.")
            }
        }
    }

    fun fun16() {
        runBlocking {
            try {
                listOf(1, 2, 3, 4, 5, 6)
                    .asFlow()
                    .map { it }
                    .onEach {
                        delay(2_000)
                        check(
                            value = it <= 4,
                            lazyMessage = {
                                printRed("check entire ...")
                                "Entire is equal or bigger than four !!!"
                            })
                        printBlue("privateFun5 Emit $it")
                    }
                    .collect {
                        printYellow("Collect $it")
                        delay(1_000)
                        printYellow("Processed $it")
                    }
            } catch (e: IllegalStateException) {
                printYellow("An exception occurred. message = ${e.message}")
            }
        }
    }

    fun fun17() {
        runBlocking {
            listOf(1, 2, 3, 4, 5, 6)
                .asFlow()
                .map { it }
                .onEach {
                    delay(2_000)
                    check(
                        value = it <= 4,
                        lazyMessage = {
                            printRed("check entire ...")
                            "Entire is equal or bigger than four !!!"
                        })
                    printBlue("privateFun5 Emit $it")
                }
                .catch {
                    printYellow("An exception occurred. message = ${it.message}")
                }
                .collect {
                    printYellow("Collect $it")
                    delay(1_000)
                    printYellow("Processed $it")
                }
        }
    }


    /**
     * * Flow completion (normally or exceptionally)
     * * We can handle flow collection completion imperative or declarative.
     * * imperative: by try_finally block
     * * declarative: by onCompletion function
     * * We can not catch exception by onCompletion and app is crashed.
     * * If onCompletion comes after catch, throwable passed into onCompletion is null (Use it before catch)
     */

    fun fun18() {
        runBlocking {
            listOf(1, 2, 3, 4, 5, 6)
                .asFlow()
                .map { it }
                .onEach {
                    check(
                        value = it <= 4,
                        lazyMessage = {
                            printRed("check entire ...")
                            "Entire is equal or bigger than four !!!"
                        })
                    delay(2_000)
                    printBlue("privateFun5 Emit $it")
                }
                .catch {
                    printYellow("An exception occurred. message = ${it.message}")
                }
                .onCompletion {
                    printYellow("End receiving data reason = ${if (it == null) "Complete normally" else it.message}.")
                }
                .collect {
                    printYellow("Collect $it")
                    delay(1_000)
                    printYellow("Processed $it")
                }
        }
    }


    /**
     * * By replacing collect with launchIn we can launch a collection of the flow in a separate coroutine.
     * * By onEach function we can register a piece of code with a reaction for incoming events.
     * * The required parameter to launchIn must specify a CoroutineScope in which the coroutine to collect the flow is launched.
     * * launchIn also returns a Job.
     */

    fun fun19() {
        runBlocking {
            val job = privateFun3()
                .onEach {
                    printYellow("Collect $it")
                    delay(1_000)
                    printYellow("Processed $it")
                }.launchIn(this)
        }
    }


    private fun privateFun1(): List<Int> {
        val list: MutableList<Int> = mutableListOf()

        for (i in 1..5) {
            Thread.sleep(1_000)
            list.add(i)
        }

        return list
    }

    private fun privateFun2(): Sequence<Int> {
        return sequence {
            for (i in 1..5) {
                Thread.sleep(1_000)
                yield(i)
            }
        }
    }

    private fun privateFun3(): Flow<Int> {
        return flow {
            for (i in 1..5) {
                delay(5_00)
                printWhite("privateFun3 Emit $i")
                emit(i)
            }
        }
    }

    private fun privateFun4(): Flow<Int> {
        return flow {
            for (i in 1..5) {
                delay(2_000)
                printYellow("privateFun4 Emit $i")
                emit(i)
            }
        }.flowOn(Dispatchers.Default)
    }

    private fun privateFun5(): Flow<String> =
        listOf("one", "two", "three", "four", "five")
            .asFlow()
            .map { it }
            .onEach {
                delay(2_000)
                printBlue("privateFun5 Emit $it")
            }
            .flowOn(Dispatchers.Default)
}
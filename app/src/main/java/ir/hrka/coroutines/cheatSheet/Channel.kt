package ir.hrka.coroutines.cheatSheet

import ir.hrka.coroutines.helpers.Log.printBlue
import ir.hrka.coroutines.helpers.Log.printRed
import ir.hrka.coroutines.helpers.Log.printWhite
import ir.hrka.coroutines.helpers.Log.printYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Channel {

    /**
     * * A Channel is conceptually very similar to BlockingQueue.
     * * Channels can be used for communication and synchronization between coroutines.
     * * Send: Sends an element to the channel, suspending the sender coroutine if the channel is full.
     * * Receive: Receives an element from the channel, suspending the receiver coroutine if the channel is empty.
     * * Close: Closes the channel, indicating that no more elements will be sent and channel closed and pass exception as a parameter.
     */

    fun fun1() {
        val channel = Channel<String>()

        runBlocking {
            launch {
                privateFun1()
                    .collect {
                        printWhite("Collect $it")
                        delay(1_000)
                        printYellow("Processed $it")
                        channel.send(it)
                        printYellow("processed value ($it) send in channel.")
                    }
                channel.close(cause = ClosedSendChannelException("End of collection and channel closed."))
            }

            launch {
                privateFun1()
                    .collect {
                        printRed("Collect $it in another coroutine.")
                        delay(1_000)
                        printRed("Processed $it in another coroutine.")
                    }
            }

            launch {
                while (true) {
                    try {
                        val recVal = channel.receive()
                        delay(5_000)
                        printRed("Received value ($recVal) from channel processed.")
                    } catch (e: Exception) {
                        printYellow("${e.javaClass.simpleName}: ${e.message.toString()}")
                        break
                    }
                }
            }
        }

        printYellow("End of computation")
    }


    /**
     * * There is a convenient coroutine builder named produce that makes it easy to do it right on producer side,
     * and an extension function consumeEach, that replaces a for loop on the consumer side.
     */


    fun fun2() {
        runBlocking {
            producer().consumeEach {
                delay(1_000)
                printRed("$it Received from the producer1 channel and consume it.")
            }
        }
    }


    /**
     * * You can create pipelines by producer consumer.
     */

    fun fun3() {
        runBlocking {
            pipeline().consumeEach {
                printRed("Receive $it from the channel and consume it.")
            }
        }
    }


    /**
     * * Fan-out: Multiple coroutines may receive from the same channel, distributing work between themselves.
     * * Note that cancelling a producer coroutine closes its channel,
     * thus eventually terminating iteration over the channel that receivers coroutines are doing.
     */


    fun fun4() {
        runBlocking {
            // one producer and three consumer
            val producer = producer()
            repeat(3) {
                createReceiver(it, producer)
            }
        }
    }


    /**
     * * Fan-in: Multiple coroutines may send to the same channel.
     * * Unbuffered channels transfer elements when sender and receiver meet each other (aka rendezvous).
     * If send is invoked first, then it is suspended until receive is invoked,
     * if receive is invoked first, it is suspended until send is invoked.
     * * Buffer allows senders to send multiple elements before suspending
     * * Send and receive operations to channels are fair with respect to the order of their invocation from multiple coroutines.
     * * Ticker channel is a special rendezvous channel that produces Unit every time
     * given delay passes since last consumption from this channel.
     */


    fun fun5() {
        runBlocking {
            // one Consumer and multi producer
            val channel = Channel<String>(capacity = 3)

            launch {
                repeat(30) {
                    delay(500)
                    createChannelProducer(it, channel)
                }
            }

            launch {
                delay(10_000)
                printBlue("Start to receive")
                for(data in channel) {
                    delay(2_000)
                    printBlue(data)
                }
            }
        }
    }


    private fun privateFun1(): Flow<String> {
        return listOf("one", "two", "three", "four", "five")
            .asFlow()
            .onEach {
                delay(500)
                printBlue("privateFun1 emit $it.")
            }
            .buffer()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.producer(): ReceiveChannel<String> {
        return produce {
            privateFun1()
                .collect {
                    printYellow("Collect $it")
                    delay(4_000)
                    send(it)
                    printYellow("Produce $it and send to the channel.")
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.pipeline(): ReceiveChannel<Int> {
        return produce {
            producer().consumeEach {
                printBlue("Collect $it")
                delay(2_000)
                val product = when (it) {
                    "one" -> 1
                    "two" -> 2
                    "three" -> 3
                    "four" -> 4
                    "five" -> 5
                    else -> 0
                }
                send(product)
                printYellow("Produce $product and send to the channel.")
            }
        }
    }

    private fun CoroutineScope.createReceiver(id: Int, channel: ReceiveChannel<String>) {
        launch {
            for (data in channel)
                printRed("$id receive $data")
        }
    }

    private fun CoroutineScope.createChannelProducer(id: Int, channel: Channel<String>) {
        launch {
            printYellow("send $id")
            channel.send("$id")
            printRed("$id received")
        }
    }
}
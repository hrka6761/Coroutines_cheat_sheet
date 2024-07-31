package ir.hrka.coroutines

import android.os.Bundle
import androidx.activity.ComponentActivity
import ir.hrka.coroutines.cheatSheet.Channel
import ir.hrka.coroutines.cheatSheet.ExceptionHandling

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        RunBlocking().fun1()
//        CoroutineScopeFunction().fun2()
//        CoroutineContextAndDispatchers().fun1()
//        CancellationCoroutines().fun8()
//        AsyncFunction().fun2()
//        AsynchronousFlow().fun19()
//        Channel().fun5()
        ExceptionHandling().fun8()
    }
}
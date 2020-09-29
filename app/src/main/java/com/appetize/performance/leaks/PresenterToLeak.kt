package com.appetize.performance.leaks

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by Zach Nwabudike on 9/28/20.
 */
//Effectively, this is what you'd get from JavaPresenterBase.java
abstract class PresenterBase(private var anotherLeakableReferenceToViewActivity: CommonViewInterface?) {

    val compositeDisposable = CompositeDisposable()

    /**
     * This does the right thing but we might miss it in the implmentation
     */
    open fun destroy() {
        anotherLeakableReferenceToViewActivity = null

    }
}


/**
 * TODO - Finish after a nap
 */
class LeakyRxPresenter(
    private val leakyView: CommonViewInterface
) : JavaPresenterBase(leakyView) {

    private fun executeRunnableWithRX() {
        println("called executeRunnableWithRX")
        invokeInteractor()
        //cause leak
        onFinishedWithFragment()
    }

    override fun leakTheViewWithMemberRunnable() {
        capturingRunnable.run()
        //cause leak
        onFinishedWithFragment()
    }

    override fun leakTheViewWithAnonymousRunnable() {
        super.getAnonymousRunnable().run()
    }

    override fun invokeInteractor() {
        compositeDisposable.add(
            leakyRxInteractor
                .invoke()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    Runnable {
                        println("capturingRunnable executed")
                        println("capturingRunnable sleeping")
                        Thread.sleep(20000)
                        println("capturingRunnable finished sleeping")
                        capturingFunctionWhenCalledFromAnonymousClass()
                        accessViewFromParent()
                        println("capturingRunnable complete")
                    }
                }
                .subscribeBy(
                    onSuccess = { result ->
                        {
                            leakTheViewWithAnonymousRunnable()
                        }
                    },
                    onError = { throwable ->
                        {
                            leakTheViewWithAnonymousRunnable()
                        }
                    }
                )
        )
        //cause leak
        onFinishedWithFragment()
    }


}

/**
 * Presenter implementation designed with bad MVP architecture
 * so that it contributes to com.appetize.performance.leaks in different ways.
 */
@Suppress("SameParameterValue")
class LeakyPresenter constructor(
    private val leakyView: CommonViewInterface
) :
    JavaPresenterBase(leakyView) {

    override fun destroy() {
        compositeDisposable.clear()
    }

    /**
     * Anonymous class com.appetize.performance.leaks view.
     * Public so the view can find it
     */
    public override fun leakTheViewWithAnonymousRunnable() {
        println("called leakTheViewWithAnonymousRunnable")

        //anonymous capturing runnable wth references to enclosing class and view
        capturingRunnable = Runnable {
            println("Runnable started")
            println("Runnable sleeping")
            Thread.sleep(20000)
            println("Runnable finished sleeping")
            capturingFunctionWhenCalledFromAnonymousClass()
            superClassWillLeak()
        }
        invokeInteractor()
        onFinishedWithFragment()
    }


    override fun invokeInteractor() {
        leakyInteractor.invoke(
            //member that is a capturing runnable wth references to enclosing class and view
            onSuccess = { executeRunnableWithThread(capturingRunnable) },
            onFailure = {
                //we don't actually call this in demo
                onFinishedWithFragment()
            }
        )
    }


    // implemented in the base presenter, if the implementor is not careful they might leak
    // don't trust the implementor with this
    private fun superClassWillLeak() {
        super.accessViewFromParent()
    }
}


/*
Remember SOLID principles
 */

//Stores results
sealed class DumbInteractorResult {
    data class Success(val name: String = "Success") : DumbInteractorResult()
    data class Fail(val error: Exception) : DumbInteractorResult()
}

//Just an interface
interface DumbInteractor {
    operator fun invoke(
        onSuccess: (DumbInteractorResult.Success) -> Unit,
        onFailure: (DumbInteractorResult.Fail) -> Unit
    )
}

//if this was a lib it would need to implement RX
interface DumbRxInteractor {
    operator fun invoke(): Single<DumbInteractorResult>
}

// This interactor shouldn't care about having nullable receivers
class LeakyInteractor : DumbInteractor {
    override operator fun invoke(
        onSuccess: (DumbInteractorResult.Success) -> Unit,
        onFailure: (DumbInteractorResult.Fail) -> Unit
    ) {
        onSuccess(DumbInteractorResult.Success("Failure is not an option"))
    }
}

// This interactor shouldn't care about having nullable receivers
class LeakyRxInteractor : DumbRxInteractor {
    override fun invoke(): Single<DumbInteractorResult> =
        Single.fromCallable {
            DumbInteractorResult.Success("does this leak")
        }
}



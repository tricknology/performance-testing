package com.appetize.performance.leaks;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.disposables.CompositeDisposable;

import static java.sql.DriverManager.println;

/**
 * Created by Zach Nwabudike on 9/28/20.
 */
public abstract class JavaPresenterBase {

    private CommonViewInterface hiddenViewToLeak;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    protected Runnable capturingRunnable = getAnonymousRunnable();

    public JavaPresenterBase(@Nullable CommonViewInterface view) {
        System.out.println("Created JavaPresenterBase");
        this.hiddenViewToLeak = view;
    }

    // will leak if implemented incorrectly
    abstract void leakTheViewWithAnonymousRunnable();

    protected LeakyInteractor leakyInteractor = new LeakyInteractor();
    protected LeakyRxInteractor leakyRxInteractor = new LeakyRxInteractor();

    private ArrayList<String> arrayRefToLeak = new ArrayList<>(Collections.singletonList("random initial contents"));

    private String capturedString = "this is capturing presenter";

    // not abstract so it doesn't yell at us like an interface will but this is bad.
    // force the implementor to implement it with annotation:
    // @CallSuper
    public void destroy() {
        // oops I didn't null the view

        //clear the subscriptions
        compositeDisposable.clear();
    }

    //calls view delegate to remove the fragment
    protected void onFinishedWithFragment() {
        println("removeCurrentFragment called");
        hiddenViewToLeak.removeCurrentFragment();
    }

    // if the implementor is not careful they might leak
    // don't trust the implementor with this
    public void accessViewFromParent() {
        System.out.println("changeTextFromSuperViewRefLeak called");
        hiddenViewToLeak.getSomeTextFromContext();
    }

    public void leakTheViewWithMemberRunnable() {
        println("called leakTheViewWithMemberRunnable");
        invokeInteractor();
        //cause leak
        onFinishedWithFragment();
    }

    protected void executeRunnableWithThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    protected abstract void invokeInteractor();

    protected void capturingFunctionWhenCalledFromAnonymousClass() {
        println("capturingFunctionWhenCalledFromAnonymousClass called");
        changeTextUsingLocalViewRefLeak(capturedString);
        arrayRefToLeak.add("we came back and called changeTextAndModifyArray");
    }

    // references the view and will leak if included in a capture
    // because this has a reference to the presenter.
    protected void changeTextUsingLocalViewRefLeak(String message) {
        println("changeTextUsingLocalViewRefLeak called to change text to $message");

        println("adding the message to the array - $message");

        arrayRefToLeak.add(String.format("%s", hiddenViewToLeak.getSomeTextFromContext()));
    }

    @NotNull
    public Runnable getAnonymousRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                println("capturingRunnable executed");
                println("capturingRunnable sleeping");
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                println("capturingRunnable finished sleeping");
                capturingFunctionWhenCalledFromAnonymousClass();
                accessViewFromParent();
                println("capturingRunnable complete");
            }
        };
    }
}

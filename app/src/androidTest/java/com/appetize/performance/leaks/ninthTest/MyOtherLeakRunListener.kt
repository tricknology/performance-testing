package com.appetize.performance.leaks.ninthTest

import leakcanary.FailTestOnLeakRunListener
import org.junit.runner.Description

@Suppress("unused") // it's implemented in app/build.gradle
class MyOtherLeakRunListener: FailTestOnLeakRunListener() {
    override fun skipLeakDetectionReason(description: Description): String? {
        return if(description.getAnnotation(LeakTest::class.java) != null)
            null
        else
            "Skip Leak test"
    }
}
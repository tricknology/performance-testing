package com.appetize.performance.leaks.eighthTest

import leakcanary.FailTestOnLeakRunListener
import org.junit.runner.Description

class MyLeakRunListener: FailTestOnLeakRunListener() {
    override fun skipLeakDetectionReason(description: Description): String? {
        return description.getAnnotation(IgnoreLeaks::class.java)?.message
    }
}
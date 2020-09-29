package com.appetize.performance.leaks.eighthTest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class IgnoreLeaks(
    val message: String = ""
)
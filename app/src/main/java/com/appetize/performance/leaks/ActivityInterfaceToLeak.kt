package com.appetize.performance.leaks

/**
 * Created by Zach Nwabudike on 9/28/20.
 */
interface CommonViewInterface {
    fun getSomeTextFromContext(): String
    fun cleanup()
    fun removeCurrentFragment(): Boolean
    fun loadLeakyFragment()
    fun changeViewText(message: String)
}

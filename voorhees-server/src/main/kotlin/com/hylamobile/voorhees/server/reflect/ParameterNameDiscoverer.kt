package com.hylamobile.voorhees.server.reflect

import java.lang.reflect.Method

interface ParameterNameDiscoverer {

    fun parameterNames(method: Method): Array<String>?
}

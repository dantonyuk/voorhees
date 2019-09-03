package com.hylamobile.voorhees.server.annotations

import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class JsonRpcService(val locations: Array<String>)

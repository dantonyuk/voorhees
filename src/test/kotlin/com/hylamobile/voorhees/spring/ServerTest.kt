package com.hylamobile.voorhees.spring

import com.hylamobile.voorhees.jsonrpc.InternalErrorException
import com.hylamobile.voorhees.server.annotations.DontExpose
import com.hylamobile.voorhees.server.annotations.JsonRpcService
import com.hylamobile.voorhees.server.annotations.Param

@JsonRpcService(["/test"])
class TestService {

    fun plus(l: Int, r: Int) = l + r

    fun replicate(
        @Param(name = "str") s: String,
        @Param(defaultValue = "2") times: Int) =
        s.repeat(times)

    fun replicate2(
        @Param(name = "str") s: String,
        @Param(defaultValue = "2") times: Int) =
        s.repeat(times)

    fun theAnswer() = 42

    fun breakALeg(): String = throw InternalErrorException("A leg is broken")

    fun breakAnArm(): String = throw RuntimeException("An arm is broken")

    fun birthday(person: Person) = person.copy(age = person.age + 1)

    fun birthdays(people: List<Person>) = people.map { it.copy(age = it.age + 1) }

    @DontExpose
    fun unexposed(): String = "the-password"
}

data class Person(val name: String, val age: Int)

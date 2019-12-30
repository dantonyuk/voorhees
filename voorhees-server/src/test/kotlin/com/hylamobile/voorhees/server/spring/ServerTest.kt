package com.hylamobile.voorhees.server.spring

import com.hylamobile.voorhees.jsonrpc.InternalErrorException
import com.hylamobile.voorhees.server.annotation.DontExpose
import com.hylamobile.voorhees.server.annotation.JsonRpcService
import com.hylamobile.voorhees.server.annotation.Param
import com.hylamobile.voorhees.server.spring.webmvc.JsonRpcHandlerMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import javax.annotation.PostConstruct

@JsonRpcService(["/test"])
@Suppress("UNUSED")
class TestService {

    fun plus(l: Int, r: Int) = l + r

    fun replicate(
        @Param(name = "str", defaultValue = "xa") s: String,
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

    fun checkNullDefaultValues(@Param(defaultValue = "null") test: String?) =
        test == null
}

@JsonRpcService(["/secured"])
@Suppress("UNUSED")
class SecuredService {

    fun secret() = "password"
}

@Suppress("UNUSED")
class ManuallyRegisteredService {

    fun ping() = "pong"
}

@Suppress("UNUSED")
@JsonRpcService(["/prefix"], prefix = "test")
class PrefixedService {

    fun plus(a: Int, b: Int) = a + b
}

@Configuration
@EnableWebSecurity
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var jsonRpcMapping: JsonRpcHandlerMapping

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/secured").hasRole("ADMIN")
                .anyRequest().permitAll().and()
            .httpBasic()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
            .withUser("admin").password("{noop}password").roles("ADMIN").and()
            .withUser("user").password("{noop}password").roles("USER")
    }

    @PostConstruct
    fun init() {
        jsonRpcMapping.registerService(ManuallyRegisteredService(), "/manual")
        jsonRpcMapping.registerService(ManuallyRegisteredService(), "/manual-prefix", "manual")
    }
}

data class Person(var name: String, var age: Int) {
    @Suppress("UNUSED")
    constructor() : this("", 0)
}

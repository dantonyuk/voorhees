package com.hylamobile.voorhees.client.spring

import com.google.common.base.CaseFormat
import com.hylamobile.voorhees.client.JsonRpcClient
import com.hylamobile.voorhees.client.spring.user.FirstUserService
import com.hylamobile.voorhees.client.spring.user.SecondUserService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class AutoRegistrationTest {

    @field:Autowired(required = false)
    private lateinit var defaultJsonRpcClient: JsonRpcClient;

    @field:Autowired
    private lateinit var userServiceJsonRpcClient: JsonRpcClient;

    @field:Autowired
    private lateinit var firstService: FirstService;

    @field:Autowired
    private lateinit var secondService: SecondService;

    @field:Autowired
    private lateinit var firstUserService: FirstUserService;

    @field:Autowired
    private lateinit var secondUserService: SecondUserService;

    @Test
    fun `clients should be registered`() {
        assertNotNull(defaultJsonRpcClient)
        assertNotNull(userServiceJsonRpcClient)
    }

    @Test
    fun `services should be registered`() {
        assertNotNull(firstService)
        assertNotNull(secondService)
        assertNotNull(firstUserService)
        assertNotNull(secondUserService)
    }
}

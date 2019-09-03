package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.module.SimpleModule
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.lang.reflect.Type
import com.fasterxml.jackson.databind.ObjectMapper



class JsonRpcIdSerializer : StdSerializer<Id<*>>(Id::class.java) {

    override fun serialize(value: Id<*>?, gen: JsonGenerator, provider: SerializerProvider?) {
        if (value == null) {
            gen.writeNull()
        }
        else {
            when (value) {
                is StringId -> gen.writeString(value.id)
                is NumberId -> gen.writeNumber(value.id)
            }
        }
    }
}

class JsonRpcIdDeserializer : StdDeserializer<Id<*>>(Id::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Id<*>? {
        return when (val parsed = p.readValueAsTree<TreeNode>()) {
            is NullNode -> null
            is TextNode -> StringId(parsed.textValue())
            is FloatNode, is DoubleNode -> throw InvalidRequestException("id SHOULD NOT contain fractional parts")
            is NumericNode -> NumberId(parsed.longValue())
            else -> throw InvalidRequestException("id MUST contain a String, Number, or NULL value")
        }
    }
}

class VersionSerializer : StdSerializer<Version>(Version::class.java) {

    override fun serialize(value: Version?, gen: JsonGenerator, provider: SerializerProvider?) {
        require (value != null) { "Version should be defined" }
        require (value == Version.ver2_0) { "Only version 2.0 is supported" }

        gen.writeString(value.version)
    }
}

class VersionDeserializer : StdDeserializer<Version>(Version::class.java) {

    private val nodeVer2_0 = TextNode.valueOf("2.0")

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Version {
        return when (p.readValueAsTree<TreeNode>()) {
            nodeVer2_0 -> Version.ver2_0
            else -> throw InvalidRequestException("Only version 2.0 is supported")
        }
    }
}

class ParamsSerializer : StdSerializer<Params>(Params::class.java) {

    override fun serialize(value: Params?, gen: JsonGenerator, provider: SerializerProvider?) {
        val data: Any? = when (value) {
            null -> null
            is ByNameParams -> value.params
            is ByPositionParams -> value.params
        }
        gen.writeObject(data)
    }
}

class ParamsDeserializer : StdDeserializer<Params>(Params::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Params? {
        return when (val node = p.readValueAsTree<JsonNode>()) {
            NullNode.instance -> null
            is ArrayNode -> ByPositionParams(node.elements().asSequence().toList())
            is ObjectNode -> ByNameParams(node.fields().asSequence().map { e -> e.key to e.value }.toMap())
            else -> throw InvalidRequestException("Params MUST be null, array, or object")
        }
    }
}

class ErrorDeserializer : StdDeserializer<Error>(Error::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Error? {
        return when (val node = p.readValueAsTree<JsonNode>()) {
            NullNode.instance -> null
            is ObjectNode -> Error(
                (node["code"] as IntNode).intValue(),
                node["message"].textValue(),
                node["data"])
            else -> throw InvalidRequestException("Error MUST be either null or object")
        }
    }
}

object Json {

    val objectMapper = ObjectMapper().
        registerModule(
            SimpleModule()
                .addSerializer(Id::class.java, JsonRpcIdSerializer())
                .addDeserializer(Id::class.java, JsonRpcIdDeserializer())
                .addSerializer(Version::class.java, VersionSerializer())
                .addDeserializer(Version::class.java, VersionDeserializer())
                .addSerializer(Params::class.java, ParamsSerializer())
                .addDeserializer(Params::class.java, ParamsDeserializer())
                .addDeserializer(Error::class.java, ErrorDeserializer())
        )

    fun readRequest(reader: Reader): Request =
        objectMapper.readValue(reader, Request::class.java)

    fun writeRequest(request: Request, writer: Writer) {
        objectMapper.writeValue(writer, request)
    }

    fun serializeRequest(request: Request): String =
        StringWriter().run {
            writeRequest(request, this)
            toString()
        }

    fun readResponse(reader: Reader): Response<*> =
        objectMapper.readValue(reader, Response::class.java)

    fun writeResponse(response: Response<*>, writer: Writer) {
        objectMapper.writeValue(writer, response)
    }

    fun serializeResponse(response: Response<*>): String =
        StringWriter().run {
            writeResponse(response, this)
            toString()
        }

    fun <T> parse(json: String, type: Type): T {
        val javaType = objectMapper.typeFactory.constructType(type)
        return objectMapper.readValue(json, javaType)
    }

    fun parseNode(node: TreeNode, type: Type): Any? {
        val parser = objectMapper.treeAsTokens(node)
        val javaType = objectMapper.typeFactory.constructType(type)
        return objectMapper.readerFor(javaType).readValue(parser)
    }

    fun parseTree(json: String): TreeNode? {
        val parser = objectMapper.factory.createParser(json)
        return objectMapper.readTree<TreeNode>(parser)
    }
}

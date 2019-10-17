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
import java.io.Writer
import java.lang.reflect.Type
import com.fasterxml.jackson.databind.ObjectMapper

private class JsonRpcIdSerializer : StdSerializer<Id<*>>(Id::class.java) {

    override fun serialize(value: Id<*>?, gen: JsonGenerator, provider: SerializerProvider?) {
        when (value) {
            null -> gen.writeNull()
            is StringId -> gen.writeString(value.id)
            is NumberId -> gen.writeNumber(value.id)
        }
    }
}

private class JsonRpcIdDeserializer : StdDeserializer<Id<*>>(Id::class.java) {

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

private class VersionSerializer : StdSerializer<Version>(Version::class.java) {

    override fun serialize(value: Version?, gen: JsonGenerator, provider: SerializerProvider?) {
        require (value != null) { "Version should be defined" }
        require (value == Version.ver2_0) { "Only version 2.0 is supported" }

        gen.writeString(value.version)
    }
}

private class VersionDeserializer : StdDeserializer<Version>(Version::class.java) {

    private val nodeVer2_0 = TextNode.valueOf("2.0")

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Version {
        return when (p.readValueAsTree<TreeNode>()) {
            nodeVer2_0 -> Version.ver2_0
            else -> throw InvalidRequestException("Only version 2.0 is supported")
        }
    }

    override fun getNullValue(ctxt: DeserializationContext?): Version {
        throw InvalidRequestException("Only version 2.0 is supported")
    }
}

private class ParamsSerializer : StdSerializer<Params>(Params::class.java) {

    override fun serialize(value: Params?, gen: JsonGenerator, provider: SerializerProvider?) {
        val data: Any? = when (value) {
            null -> null
            is ByNameParams -> value.params
            is ByPositionParams -> value.params
        }
        gen.writeObject(data)
    }
}

private class ParamsDeserializer : StdDeserializer<Params>(Params::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Params? {
        return when (val node = p.readValueAsTree<JsonNode>()) {
            NullNode.instance -> null
            is ArrayNode -> ByPositionParams(node.elements().asSequence().toList())
            is ObjectNode -> ByNameParams(node.fields().asSequence().map { e -> e.key to e.value }.toMap())
            else -> throw InvalidRequestException("Params MUST be null, array, or object")
        }
    }
}

private class ErrorDeserializer : StdDeserializer<Error>(Error::class.java) {

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

private val objectMapper = ObjectMapper().
    registerModule(
        SimpleModule()
            .addSerializer(Id::class.java, JsonRpcIdSerializer())
            .addDeserializer(Id::class.java, JsonRpcIdDeserializer())
            .addSerializer(Version::class.java, VersionSerializer())
            .addDeserializer(Version::class.java, VersionDeserializer())
            .addSerializer(Params::class.java, ParamsSerializer())
            .addDeserializer(Params::class.java, ParamsDeserializer())
            .addDeserializer(Error::class.java, ErrorDeserializer()))

val Any?.jsonString
    get() = objectMapper.writeValueAsString(this)

val Any?.jsonTree: JsonNode
    get() = objectMapper.valueToTree(this) ?: NullNode.instance

fun Reader.readRequest(): Request =
    objectMapper.readValue(this, Request::class.java)

fun Writer.writeResponse(response: Response<*>) =
    objectMapper.writeValue(this, response)

fun String.parseRequest(): Request = parseJsonAs(Request::class.java) as Request

fun String.parseResponse(): Response<*> = parseJsonAs(Response::class.java) as Response<*>

fun String.parseJsonAs(type: Type): Any? {
    val javaType = objectMapper.typeFactory.constructType(type)
    return objectMapper.readValue(this, javaType)
}

fun <T> TreeNode?.parseAs(type: Type): T? {
    if (this == null) return null
    val parser = objectMapper.treeAsTokens(this)
    val javaType = objectMapper.typeFactory.constructType(type)
    return objectMapper.readerFor(javaType).readValue(parser) as T?
}

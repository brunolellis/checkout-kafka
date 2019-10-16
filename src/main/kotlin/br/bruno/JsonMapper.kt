package br.bruno

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonMapper {
    private val JSON_MAPPER = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        registerKotlinModule()
    }

    fun writeAsString(obj: Any) = JSON_MAPPER.writeValueAsString(obj)

    fun writeAsBytes(obj: Any) = JSON_MAPPER.writeValueAsBytes(obj)

    fun <T> readString(str: String, clazz: Class<T>) = JSON_MAPPER.readValue(str, clazz)

    fun <T> readBytes(data: ByteArray, clazz: Class<T>) = JSON_MAPPER.readValue(data, clazz)

}
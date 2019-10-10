package br.bruno.orders

import br.bruno.checkout.Order
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Serializer

class OrderSerializer : Serializer<Order> {

    companion object {
        val JSON_MAPPER = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            registerKotlinModule()
        }
    }

    override fun serialize(topic: String, data: Order): ByteArray {
        return JSON_MAPPER.writeValueAsBytes(data)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}
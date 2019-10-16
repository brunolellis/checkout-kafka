package br.bruno.orders

import br.bruno.JsonMapper
import br.bruno.checkout.Order
import org.apache.kafka.common.serialization.Deserializer

class OrderDeserializer : Deserializer<Order> {

    override fun deserialize(topic: String, data: ByteArray): Order {
        return JsonMapper.readBytes(data, Order::class.java)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}
package br.bruno.orders

import br.bruno.JsonMapper
import br.bruno.checkout.Order
import org.apache.kafka.common.serialization.Serializer

class OrderSerializer : Serializer<Order> {

    override fun serialize(topic: String, data: Order): ByteArray {
        return JsonMapper.writeAsBytes(data)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}
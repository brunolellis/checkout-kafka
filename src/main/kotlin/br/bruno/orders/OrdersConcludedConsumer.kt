package br.bruno.orders

import br.bruno.checkout.Order
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
    OrdersConcludedConsumer("localhost:9092", "orders-concluded").consume()
}

class OrdersConcludedConsumer(private val brokers: String, private val topic: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun consume() {
        val consumer = createConsumer(brokers)
        consumer.subscribe(listOf(topic))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            records.iterator().forEach {
                val order = it.value()
                println("processing CONCLUDED order $order")
            }
        }
    }

    private fun createConsumer(brokers: String): Consumer<String, Order> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "order-processor" // CONSUMER GROUP! up to 4 in parallel (see --partitions topic)
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = OrderDeserializer::class.java
        return KafkaConsumer<String, Order>(props)
    }

}
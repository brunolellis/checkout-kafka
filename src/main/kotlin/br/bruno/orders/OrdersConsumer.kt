package br.bruno.orders

import br.bruno.checkout.Order
import br.bruno.checkout.OrderStatus
import br.bruno.checkout.createProducer
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
    println("placed orders")

    val brokers = "localhost:9092"
    val consumer = createConsumer(brokers)
    consumer.subscribe(listOf("orders"))

    val concludedOrdersProducer = createProducer(brokers)

    while (true) {
        val records = consumer.poll(Duration.ofSeconds(1))
        records.iterator().forEach {
            val order = it.value()
            println("processing order $order")

            if (order.status == OrderStatus.CONCLUDED) {
                println("sending concluded order: $order")
                val futureResult = concludedOrdersProducer.send(ProducerRecord("orders-concluded", order))
                futureResult.get()
            }
        }
    }
}

private fun createConsumer(brokers: String): Consumer<String, Order> {
    val props = Properties()
    props["bootstrap.servers"] = brokers
    props["group.id"] = "order-processor-1" // CONSUMER GROUP! up to 4 in parallel (see --partitions topic)
    props["key.deserializer"] = StringDeserializer::class.java
    props["value.deserializer"] = OrderDeserializer::class.java
    return KafkaConsumer<String, Order>(props)
}


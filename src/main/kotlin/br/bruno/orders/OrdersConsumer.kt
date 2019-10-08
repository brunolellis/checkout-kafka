package br.bruno.orders

import br.bruno.checkout.Order
import br.bruno.checkout.OrderStatus
import br.bruno.checkout.createProducer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.javafaker.Faker
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.time.Instant
import java.util.*

fun main(args: Array<String>) {
    println("placed orders")

    val jsonMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerKotlinModule()
    }

    val brokers = "localhost:9092"
    val consumer = createConsumer(brokers)
    consumer.subscribe(listOf("orders"))

    val concludedOrdersProducer = createProducer(brokers)

    while (true) {
        val records = consumer.poll(Duration.ofSeconds(1))
        records.iterator().forEach {
            val json = it.value()
            println("processing order $json")
            val order = jsonMapper.readValue(json, Order::class.java)

            if (order.status == OrderStatus.CONCLUDED) {
                println("sending concluded order: $json")
                val futureResult = concludedOrdersProducer.send(ProducerRecord("orders-concluded", json))
                futureResult.get()
            }
        }
    }
}

private fun createConsumer(brokers: String): Consumer<String, String> {
    val props = Properties()
    props["bootstrap.servers"] = brokers
    props["group.id"] = "order-processor-1" // CONSUMER GROUP! up to 4 in parallel (see --partitions topic)
    props["key.deserializer"] = StringDeserializer::class.java
    props["value.deserializer"] = StringDeserializer::class.java
    return KafkaConsumer<String, String>(props)
}


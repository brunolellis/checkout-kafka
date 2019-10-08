package br.bruno.checkout

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.javafaker.Faker
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Instant
import java.util.*


fun main(args: Array<String>) {
    println("checkout")

    val jsonMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerKotlinModule()
    }

    val producer = createProducer("localhost:9092")

    val customer1 = UUID.fromString("e27ce743-d650-497b-a6fb-55a5fd8e6ce5")
    val customer2 = UUID.fromString("3e99ba6a-3e9a-43f7-8399-7337cad4ae0c")
    val campaignId = UUID.fromString("a467e182-e86f-47df-baad-a8636a65deb8")

    val faker = Faker()

    for (i in 1..10) {
        val order = Order(
            id = UUID.randomUUID(),
            customerId = customer1,
            createdAt = Instant.now(),
            status = randomOrderStatus(),
            campaignId = campaignId,
            totalValue = faker.number().randomDouble(2, 40, 80),
            deliveryFee = faker.number().randomDouble(0, 0, 10),
            credits = faker.number().randomDouble(2, 0, 20)
        )
        val orderJson = jsonMapper.writeValueAsString(order)

        val result = placeOrder(producer, orderJson)
        println("$i) order placed $result")

        if (order.status == OrderStatus.CONFIRMED) {
            val orderConcluded = order.copy(status = OrderStatus.CONCLUDED)
            val concludedJson = jsonMapper.writeValueAsString(orderConcluded)
            placeOrder(producer, concludedJson)
        }
    }
}

private fun placeOrder(producer: Producer<String, String>, orderJson: String): RecordMetadata {
    println("placing order: $orderJson")
    val futureResult = producer.send(ProducerRecord("orders", orderJson))
    return futureResult.get()
}

private fun randomOrderStatus(): OrderStatus {
    val values = listOf(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PAID)
    return values.get(Random().nextInt(values.size));
}

fun createProducer(brokers: String): Producer<String, String> {
    val props = Properties()
    props["bootstrap.servers"] = brokers
    props["key.serializer"] = StringSerializer::class.java
    props["value.serializer"] = StringSerializer::class.java
    return KafkaProducer<String, String>(props)
}

data class Order(
    val id: UUID,
    val customerId: UUID,
    val createdAt: Instant,
    val status: OrderStatus,
    val campaignId: UUID,
    val totalValue: Double, // valor final pago pelo cliente (itens + entrega - creditos)
    val deliveryFee: Double, // valor da entrega
    val credits: Double // este valor é o que deve ser considerado para controlar o budget da campanha
)

enum class OrderStatus {
    PLACED, PAID, CONFIRMED, CONCLUDED
}
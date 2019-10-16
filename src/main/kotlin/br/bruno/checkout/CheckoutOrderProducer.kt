package br.bruno.checkout

import br.bruno.orders.OrderSerializer
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

    val producer = createProducer("localhost:9092")

    val customer1 = UUID.fromString("e27ce743-d650-497b-a6fb-55a5fd8e6ce5")
    val customer2 = UUID.fromString("3e99ba6a-3e9a-43f7-8399-7337cad4ae0c")
    val campaignId = UUID.fromString("a467e182-e86f-47df-baad-a8636a65deb8")

    val faker = Faker()

    for (i in 1..1001) {
        val order = Order(
            id = UUID.randomUUID(),
            customerId = customer1,
            createdAt = Instant.now(),
            status = OrderStatus.CONCLUDED, //randomOrderStatus(),
            campaignId = campaignId,
            totalValue = faker.number().randomDouble(2, 40, 80),
            deliveryFee = faker.number().randomDouble(0, 0, 10),
            credits = 1.0 // faker.number().randomDouble(2, 0, 20)
        )

        val result = placeOrder(producer, order)
        println("$i) order placed $result")

        if (order.status == OrderStatus.CONFIRMED) {
            val orderConcluded = order.copy(status = OrderStatus.CONCLUDED)
            placeOrder(producer, orderConcluded)
        }
    }
}

private fun placeOrder(producer: Producer<String, Order>, order: Order): RecordMetadata {
    println("placing order: $order")
    val futureResult = producer.send(ProducerRecord("orders", order))
    return futureResult.get()
}

private fun randomOrderStatus(): OrderStatus {
    val values = listOf(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PAID)
    return values.get(Random().nextInt(values.size));
}

fun createProducer(brokers: String): Producer<String, Order> {
    val props = Properties()
    props["bootstrap.servers"] = brokers
    props["key.serializer"] = StringSerializer::class.java
    props["value.serializer"] = OrderSerializer::class.java
    return KafkaProducer<String, Order>(props)
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
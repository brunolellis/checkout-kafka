package br.bruno.orders

import br.bruno.checkout.Order
import br.bruno.checkout.OrderStatus
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import java.util.*

fun main() {
    OrderConsumerStream("localhost:9092").process()
}

class OrderConsumerStream(private val brokers: String) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun process() {
        val wrappedSerDe = Serdes.WrapperSerde(OrderSerializer(), OrderDeserializer())
        val builder = StreamsBuilder()

        val ordersStream: KStream<String, Order> = builder
            .stream<String, Order>("orders", Consumed.with(Serdes.String(), wrappedSerDe))

        ordersStream.peek {_, order ->
                val now = System.currentTimeMillis()
                val createdAt = order.createdAt.toEpochMilli()

                logger.info("order ${order.id} consumed in ${now - createdAt} ms")
            }
            .filter { _, order -> order.status == OrderStatus.CONCLUDED }
            .peek { _, order -> logger.info("sending concluded order: $order") }
            .to("orders-concluded", Produced.with(Serdes.String(), wrappedSerDe))

        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["application.id"] = "order-checkout-processor"
        val streams = KafkaStreams(builder.build(), props)
        streams.start()

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(Thread(streams::close));
    }
}
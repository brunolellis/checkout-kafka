package br.bruno.orders

import br.bruno.checkout.Order
import br.bruno.checkout.OrderStatus
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import java.util.*

fun main() {
    val wrappedSerDe = Serdes.WrapperSerde(OrderSerializer(), OrderDeserializer())
    val builder = StreamsBuilder()

    val ordersStream: KStream<String, Order> = builder
        .stream<String, Order>("orders", Consumed.with(Serdes.String(), wrappedSerDe))

    ordersStream.peek {_, order -> println("processing order $order")}
        .filter { _, order -> order.status == OrderStatus.CONCLUDED }
        .peek { _, order -> println("sending concluded order: $order") }
        .to("orders-concluded", Produced.with(Serdes.String(), wrappedSerDe))

    val topology = builder.build()

    val props = Properties()
    props["bootstrap.servers"] = "localhost:9092"
    props["application.id"] = "order-checkout-processor"
    val streams = KafkaStreams(topology, props)
    streams.start()
}
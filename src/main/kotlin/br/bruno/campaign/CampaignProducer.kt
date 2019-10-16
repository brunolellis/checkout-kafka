package br.bruno.campaign

import br.bruno.JsonMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Instant
import java.util.*

fun main(args: Array<String>) {
    CampaignProducer("localhost:9092").create()
}

class CampaignProducer(private val brokers: String) {

    fun create() {
        val producer = createProducer(brokers)

        val campaignId = UUID.fromString("a467e182-e86f-47df-baad-a8636a65deb8")

        for (i in 1..1) {
            val campaign = Campaign(
                id = campaignId,
                createdAt = Instant.now(),
                status = CampaignStatus.ACTIVE,
                name = "First campaign",
                voucher = "WELCOME",
                budget = 1000.0,
                budgetSpent = 0.0,
                startDate = Instant.now(),
                endDate = Instant.now().plusSeconds(1000000000)
            )

            val result = createCampaign(producer, campaign)
            println("$i) campaign created $result")

        }
    }

    private fun createCampaign(producer: Producer<String, String>, campaign: Campaign): RecordMetadata {
        val campaignJson = JsonMapper.writeAsString(campaign)

        println("creating campaign: $campaignJson")
        val futureResult = producer.send(ProducerRecord("campaigns", campaign.id.toString(), campaignJson))
        return futureResult.get()
    }

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

}

data class Campaign(
    val id: UUID,
    val createdAt: Instant,
    val status: CampaignStatus,
    val name: String,
    val voucher: String,
    val budget: Double,
    val budgetSpent: Double,
    val startDate: Instant,
    val endDate: Instant
)

enum class CampaignStatus {
    INACTIVE, ACTIVE, EXPIRED, EXHAUSTED
}

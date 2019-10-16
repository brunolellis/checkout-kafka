# checkout

this project simulates placing random orders in an e-commerce like fashion

### kafka setup

- start zookeeper: `./bin/zookeeper-server-start.sh config/zookeeper.properties`
- start kafka: `./bin/kafka-server-start.sh config/server.properties`

- create `orders` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders --replication-factor 1 --partitions 1`
- create `orders-concluded` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders-concluded --replication-factor 1 --partitions 1`
- create `campaigns` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic campaigns --replication-factor 1 --partitions 1`

- print `orders` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders`
- print `orders-concluded` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders-concluded --from-beginning` 

### confluent platform setup

Another way to configure your setup and also use KSQL server is downloading [Confluent Platform here](https://www.confluent.io/download/).
- download package (+700mb!)
- extract and cd into `confluent-5.3.1`
- download confluent cli script: `curl -L https://cnfl.io/cli | sh -s -- -b ./bin/`
- define the following environment variables:

```
  export CONFLUENT_HOME=<DIR>/confluent-5.3.1
  export CONFLUENT_CURRENT=$CONFLUENT_HOME/var
  PATH=$CONFLUENT_HOME/bin:$PATH
```

- to start the whole platform: `confluent local start`
- or just `confluent local start ksql-server` to start ksql-server and its dependencies
- then create topics, streams etc using the same commands structure above


### KSQL

Some useful commands on ksql cli:

> LIST TOPICS;
>
> LIST STREAMS;
> 
> DESCRIBE orders_stream;
>
> SHOW QUERIES;
> 
> TERMINATE <ID>;
>
> DROP STREAM <ID>;
>
> SET 'auto.offset.reset' = 'earliest';


create `orders_stream` to read from `orders` topic:
```
CREATE STREAM orders_stream \
    (id VARCHAR, customerId VARCHAR, createdAt VARCHAR, status VARCHAR, campaignId VARCHAR, totalValue DOUBLE, deliveryFee DOUBLE, credits DOUBLE) \
    WITH (KAFKA_TOPIC='orders', VALUE_FORMAT='JSON', KEY='id');
```

create `orders_concluded_stream` to read only `CONCLUDED` orders from `orders_stream` above and sink them to `orders-concluded` topic:
```
CREATE STREAM orders_concluded_stream \
    WITH (kafka_topic='orders-concluded', value_format='JSON') AS \
    SELECT id, customerId, createdAt, status, campaignId, totalValue, deliveryFee, credits \
    FROM orders_stream \
    WHERE status='CONCLUDED';
```

create another stream (`sum_redemption_by_campaign_id_stream`):
```
CREATE TABLE sum_redemption_by_campaign_id_stream WITH (PARTITIONS=1) AS \
    SELECT campaignId, sum(credits) as budgetSpent FROM orders_stream \
    WHERE status='CONCLUDED' \
    GROUP BY campaignId;
```

create `campaigns` table:
```
CREATE TABLE campaigns \
    (id VARCHAR, createdAt VARCHAR, status VARCHAR, name VARCHAR, voucher VARCHAR, budget DOUBLE, budgetSpent DOUBLE, startDate VARCHAR, endDate VARCHAR) \
    WITH (KAFKA_TOPIC='campaigns', VALUE_FORMAT='JSON', KEY='id');
```

create a table joining order and campaign:
```
CREATE TABLE orders_concluded_with_campaigns AS \
  SELECT c.id, c.budget, s.budgetSpent
  FROM CAMPAIGNS c \
  LEFT JOIN sum_redemption_by_campaign_id_stream s on c.id = s.CAMPAIGNID;
```

create `campaigns_exhausted` table to store campaigns which budget is over:
```
CREATE TABLE campaigns_exhausted WITH (PARTITIONS=1) AS \
    SELECT id, budget, S_BUDGETSPENT FROM orders_concluded_with_campaigns \
    WHERE S_BUDGETSPENT >= budget;
```

### Steps

1. run `CampaignProducer` to create a sample campaign
2. run `CheckoutOrderProducer` to create some orders
3. start `OrderConsumerStream` (as it is an unbounded stream, it will never stop until you shut it down)
    1. `OrdersConsumer` is another way of doing the same thing
4. start `OrderConcludedConsumer`


---

* Interesting: `GEO_DISTANCE`
```
CREATE STREAM ENVIRONMENT_DATA_LOCAL_WITH_TS AS \
SELECT * FROM ENVIRONMENT_DATA_WITH_TS \
WHERE  GEO_DISTANCE(LAT,LONG,53.919066, -1.815725,'KM') < 100;
```
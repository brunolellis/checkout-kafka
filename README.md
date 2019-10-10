# checkout

this project simulates placing random orders in an e-commerce like fashion

### kafka setup

- start zookeeper: `./bin/zookeeper-server-start.sh config/zookeeper.properties`
- start kafka: `./bin/kafka-server-start.sh config/server.properties`

- create `orders` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders --replication-factor 1 --partitions 4`
- create `orders-concluded` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders-concluded --replication-factor 1 --partitions 4`

- print `orders` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders`
- print `orders-concluded` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders-concluded --from-beginning` 

### confluent platform setup

Another way to configure your setup and also use KSQL server is downloading [Confluent Platform here](https://www.confluent.io/download/).
- download package (+700mb!)
- extract and cd into `confluent-5.3.1`
- download confluent cli script: `curl -L https://cnfl.io/cli | sh -s -- -b ./bin/`
- to start the whole platform: `confluent local start`
- then create topics and etc using the same commands structure


### KSQL

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

create `orders_stream` to read from `orders` topic:
```
CREATE STREAM orders_stream \
    (id VARCHAR, customerId VARCHAR, createdAt VARCHAR, status VARCHAR, campaignId VARCHAR, totalValue DOUBLE, deliveryFee DOUBLE, credits DOUBLE) \
    WITH (KAFKA_TOPIC='orders', VALUE_FORMAT='JSON');
```

create `orders_concluded_stream` to read only `CONCLUDED` orders from `orders_stream` above:
```
CREATE STREAM orders_concluded_stream \
    WITH (kafka_topic='orders-concluded', value_format='JSON') AS \
    SELECT id, customerId, createdAt, status, campaignId, totalValue, deliveryFee, credits \
    FROM orders_stream \
    WHERE status='CONCLUDED';
```





* Interesting: `GEO_DISTANCE`
```
CREATE STREAM ENVIRONMENT_DATA_LOCAL_WITH_TS AS \
SELECT * FROM ENVIRONMENT_DATA_WITH_TS \
WHERE  GEO_DISTANCE(LAT,LONG,53.919066, -1.815725,'KM') < 100;
```
# checkout

this project simulates placing random orders in an e-commerce like fashion

### kafka setup

- start zookeeper: `./bin/zookeeper-server-start.sh config/zookeeper.properties`
- start kafka: `./bin/kafka-server-start.sh config/server.properties`

- create `orders` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders --replication-factor 1 --partitions 4`
- create `orders-concluded` topic: `./bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic orders-concluded --replication-factor 1 --partitions 4`

- print `orders` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders`
- print `orders-concluded` published messages: `./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders-concluded --from-beginning` 

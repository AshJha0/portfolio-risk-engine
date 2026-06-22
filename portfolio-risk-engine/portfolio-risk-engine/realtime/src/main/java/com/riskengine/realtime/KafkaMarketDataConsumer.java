package com.riskengine.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Consumes market data tick messages from a Kafka topic and republishes them onto the
 * {@link RealTimeRiskPipeline}'s Disruptor ring buffer for low-latency processing.
 *
 * <p>Expected message format (JSON): {@code {"symbol":"AAPL","price":201.5,"volatility":0.022}}</p>
 *
 * <p>Run this on its own dedicated thread via {@link #start()}; call {@link #stop()} for
 * graceful shutdown (it uses {@link KafkaConsumer#wakeup()} internally, which is the
 * standard safe way to interrupt a blocking {@code poll()} call).</p>
 */
public class KafkaMarketDataConsumer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(KafkaMarketDataConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final RealTimeRiskPipeline pipeline;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<String> topics;

    public KafkaMarketDataConsumer(String bootstrapServers, String groupId,
                                    List<String> topics, RealTimeRiskPipeline pipeline) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // Market data is a "latest value wins" stream; we don't need transactional exactly-once
        // semantics here, just low-latency delivery, so default auto-commit is acceptable.

        this.consumer = new KafkaConsumer<>(props);
        this.topics = topics;
        this.pipeline = pipeline;
    }

    public void start() {
        running.set(true);
        consumer.subscribe(topics);
        Thread thread = new Thread(this, "kafka-market-data-consumer");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    handleRecord(record.value());
                }
            }
        } catch (org.apache.kafka.common.errors.WakeupException e) {
            // Expected on shutdown via consumer.wakeup()
        } finally {
            consumer.close();
        }
    }

    private void handleRecord(String json) {
        try {
            MarketTickMessage msg = objectMapper.readValue(json, MarketTickMessage.class);
            pipeline.publishTick(msg.symbol, msg.price, msg.volatility);
        } catch (Exception e) {
            log.warn("Failed to parse market data message: {}", json, e);
        }
    }

    public void stop() {
        running.set(false);
        consumer.wakeup();
    }

    /** Minimal DTO matching the expected JSON tick message shape. */
    public static class MarketTickMessage {
        public String symbol;
        public double price;
        public double volatility;
    }
}

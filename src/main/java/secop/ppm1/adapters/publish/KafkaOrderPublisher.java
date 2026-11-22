// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.publish;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import secop.ppm1.domain.model.ProvisioningOrder;
import secop.ppm1.domain.port.OrderPublisher;

/**
 * Canal Kafka : les ordres sont déposés dans la file consommée par arc-publisher
 * ({@code ARC_ORDERS_CHANNEL=kafka}, par défaut). La clé du message est l'identifiant de l'ordre.
 */
@Component
@ConditionalOnProperty(name = "arc.orders.channel", havingValue = "kafka", matchIfMissing = true)
public class KafkaOrderPublisher implements OrderPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final String targetSystem;
    private final String source;

    @Autowired
    public KafkaOrderPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${arc.orders.topic}") String topic,
            @Value("${arc.publisher.target-system}") String targetSystem,
            @Value("${arc.publisher.source}") String source) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.targetSystem = targetSystem;
        this.source = source;
    }

    @Override
    public void submit(ProvisioningOrder order) {
        OrderRequestDto message = OrderRequestDto.from(order, targetSystem, source);
        message.setOperation("SUBMIT");
        send(order.id(), message);
    }

    @Override
    public void cancel(UUID orderId) {
        OrderRequestDto message = new OrderRequestDto();
        message.setOperation("CANCEL");
        message.setId(orderId);
        send(orderId, message);
    }

    private void send(UUID orderId, OrderRequestDto message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, orderId.toString(), json).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (JsonProcessingException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("The order " + orderId + " could not be published to " + topic, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing the order " + orderId, e);
        }
    }
}

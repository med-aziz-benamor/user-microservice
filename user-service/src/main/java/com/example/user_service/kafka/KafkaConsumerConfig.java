package com.example.user_service.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure comment Kafka reçoit les messages + gestion des erreurs (DLQ).
 *
 * DLQ = Dead Letter Queue
 * Si un consumer plante 3 fois sur un message → le message va dans user.events.DLQ
 * Comme ça le message "empoisonné" ne bloque pas tout le reste.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service-group");

        // Commencer depuis le début si c'est la première fois
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                   StringDeserializer.class);

        // Désérialiser le JSON → UserEvent automatiquement
        JsonDeserializer<UserEvent> deserializer =
            new JsonDeserializer<>(UserEvent.class, false);
        deserializer.addTrustedPackages("com.example.user_service.kafka");

        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            deserializer
        );
    }

    /**
     * Factory pour les @KafkaListener — avec gestion d'erreurs DLQ
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, UserEvent> consumerFactory,
            @Autowired KafkaTemplate<String, UserEvent> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // ── Configuration de la Dead Letter Queue ──
        // Si le traitement échoue → envoyer dans user.events.DLQ
        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // Tous les messages échoués → user.events.DLQ partition 0
                    return new TopicPartition("user.events.DLQ", 0);
                }
            );

        // Backoff exponentiel : attendre 1s, puis 2s, puis 4s entre les tentatives
        ExponentialBackOff backoff = new ExponentialBackOff(1000L, 2.0);
        backoff.setMaxAttempts(3); // 3 tentatives max avant DLQ

        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff));

        return factory;
    }
}

package com.example.user_service.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure comment Kafka envoie les messages.
 *
 * Clé   (key)   → String  ex: "abc123" (keycloakId)
 * Valeur (value) → UserEvent sérialisé en JSON
 *
 * Pourquoi une clé ? Kafka utilise la clé pour choisir la partition.
 * Même clé = même partition = ordre garanti pour un user donné.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, UserEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Adresse du serveur Kafka
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // La clé est une String simple
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                   StringSerializer.class);

        // La valeur (UserEvent) est convertie en JSON
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                   JsonSerializer.class);

        // "all" = attendre confirmation de tous les replicas avant de continuer
        // Garantit qu'aucun message n'est perdu
        config.put(ProducerConfig.ACKS_CONFIG, "all");

        // Réessayer 3 fois si le réseau est instable
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        // Attendre 100ms pour grouper plusieurs messages ensemble (optimisation)
        config.put(ProducerConfig.LINGER_MS_CONFIG, 100);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate = l'objet qu'on injecte dans les services
     * pour envoyer des messages. C'est l'API principale.
     */
    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

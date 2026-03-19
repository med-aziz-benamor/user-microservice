package com.example.user_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de DÉMONSTRATION — montre comment d'autres services
 * écouteraient les events publiés par UserEventProducer.
 *
 * Dans un vrai projet microservices, ce consumer serait dans
 * un service séparé (ex: notification-service, audit-service...).
 *
 * Ici on l'inclut pour montrer que le cycle publish/consume fonctionne.
 */
@Component
@Slf4j
public class UserEventConsumer {

    /**
     * Écoute le topic "user.created"
     * groupId = identifiant du groupe de consommateurs
     * (Kafka distribue les messages entre tous les membres du groupe)
     */
    @KafkaListener(
        topics = "user.created",
        groupId = "user-service-demo",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserCreated(UserEvent event) {
        log.info("[DEMO CONSUMER] ✅ User CREATED received:");
        log.info("  → userId    : {}", event.userId());
        log.info("  → email     : {}", event.email());
        log.info("  → firstName : {}", event.firstName());
        log.info("  → timestamp : {}", event.timestamp());
        // Dans un vrai service : envoyer un email de bienvenue, créer un profil...
    }

    @KafkaListener(
        topics = "user.updated",
        groupId = "user-service-demo",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserUpdated(UserEvent event) {
        log.info("[DEMO CONSUMER] 🔄 User UPDATED received:");
        log.info("  → userId    : {}", event.userId());
        log.info("  → email     : {}", event.email());
        log.info("  → timestamp : {}", event.timestamp());
        // Dans un vrai service : mettre à jour un index de recherche...
    }

    @KafkaListener(
        topics = "user.deleted",
        groupId = "user-service-demo",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserDeleted(UserEvent event) {
        log.info("[DEMO CONSUMER] 🗑️ User DELETED received:");
        log.info("  → userId     : {}", event.userId());
        log.info("  → keycloakId : {}", event.keycloakId());
        log.info("  → timestamp  : {}", event.timestamp());
        // Dans un vrai service : supprimer les données personnelles (RGPD)...
    }

    /**
     * Consumer de la Dead Letter Queue
     * Reçoit les messages qui ont échoué 3 fois dans les autres consumers
     */
    @KafkaListener(
        topics = "user.events.DLQ",
        groupId = "user-service-dlq",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeadLetter(UserEvent event) {
        // En production : alerter l'équipe, sauvegarder pour replay manuel
        log.error("[DLQ] ⚠️ Message failed after 3 attempts:");
        log.error("  → eventType : {}", event.eventType());
        log.error("  → userId    : {}", event.userId());
        log.error("  → timestamp : {}", event.timestamp());
    }
}

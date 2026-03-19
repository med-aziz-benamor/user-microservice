package com.example.userservice.kafka;

import com.example.userservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Ce service est injecté dans UserService (M4).
 * Il publie un event Kafka à chaque opération CRUD.
 *
 * kafkaTemplate.send(topic, key, value)
 *   → topic : le "canal" Kafka (user.created, user.updated, user.deleted)
 *   → key   : keycloakId (pour garantir l'ordre par user)
 *   → value : l'objet UserEvent (sérialisé en JSON automatiquement)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    // Noms des topics — constantes pour éviter les fautes de frappe
    private static final String TOPIC_CREATED = "user.created";
    private static final String TOPIC_UPDATED = "user.updated";
    private static final String TOPIC_DELETED = "user.deleted";

    /**
     * Publier un event "user créé"
     * Appelé depuis UserService.create() après le save en BDD
     */
    public void publishUserCreated(User user) {
        UserEvent event = UserEvent.created(user);

        // .send() est asynchrone — il retourne un CompletableFuture
        CompletableFuture<SendResult<String, UserEvent>> future =
            kafkaTemplate.send(TOPIC_CREATED, user.getKeycloakId(), event);

        // whenComplete est appelé quand Kafka confirme (ou refuse) le message
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                // ⚠️ Le message n'a PAS été envoyé → logguer pour investigation
                log.error("Failed to publish user.created for keycloakId={}: {}",
                          user.getKeycloakId(), ex.getMessage());
            } else {
                // ✅ Message envoyé — on log la partition et l'offset pour debug
                log.info("Published user.created | keycloakId={} | partition={} | offset={}",
                         user.getKeycloakId(),
                         result.getRecordMetadata().partition(),
                         result.getRecordMetadata().offset());
            }
        });
    }

    /**
     * Publier un event "user modifié"
     */
    public void publishUserUpdated(User user) {
        kafkaTemplate.send(TOPIC_UPDATED, user.getKeycloakId(), UserEvent.updated(user))
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish user.updated for keycloakId={}: {}",
                              user.getKeycloakId(), ex.getMessage());
                } else {
                    log.info("Published user.updated | keycloakId={}",
                             user.getKeycloakId());
                }
            });
    }

    /**
     * Publier un event "user supprimé"
     * On passe les IDs directement car l'objet User sera bientôt supprimé
     */
    public void publishUserDeleted(String userId, String keycloakId) {
        kafkaTemplate.send(TOPIC_DELETED, keycloakId, UserEvent.deleted(userId, keycloakId))
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish user.deleted for keycloakId={}: {}",
                              keycloakId, ex.getMessage());
                } else {
                    log.info("Published user.deleted | keycloakId={}", keycloakId);
                }
            });
    }
}

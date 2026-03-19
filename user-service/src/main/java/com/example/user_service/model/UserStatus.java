package com.example.user_service.model;

/**
 * Les 4 états possibles d'un utilisateur
 * Correspond au type SQL créé dans V1__create_users.sql
 */
public enum UserStatus {
    PENDING_VERIFICATION,   // Vient de s'inscrire, email pas encore vérifié
    ACTIVE,                 // Compte actif et fonctionnel
    SUSPENDED,              // Compte suspendu par un admin
    DELETED                 // Soft delete (pas vraiment supprimé de la BDD)
}

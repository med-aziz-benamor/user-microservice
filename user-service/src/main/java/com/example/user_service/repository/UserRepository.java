
package com.example.user_service.repository;

import com.example.user_service.model.User;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

/**

 * JpaRepository nous donne GRATUITEMENT : save, findById, findAll, delete...

 * On ajoute juste les méthodes spécifiques à notre besoin

 */

@Repository

public interface UserRepository extends JpaRepository<User, UUID> {

    // Spring génère le SQL automatiquement depuis le nom de la méthode !

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Recherche dans prénom ET nom (insensible à la casse)

    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(

        String firstName, String lastName, Pageable pageable);

    // Seulement les users non supprimés (deleted_at IS NULL)

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")

    Page<User> findAllActive(Pageable pageable);

}


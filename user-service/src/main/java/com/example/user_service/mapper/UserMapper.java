package com.example.user_service.mapper;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.model.User;
import org.springframework.stereotype.Component;

/**
 * Convertit User (entité BDD) → UserResponse (ce qu'on envoie au client)
 * Version manuelle — pas besoin de MapStruct
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getKeycloakId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getStatus().name(),   // Enum → String
            user.getCreatedAt()
        );
    }
}

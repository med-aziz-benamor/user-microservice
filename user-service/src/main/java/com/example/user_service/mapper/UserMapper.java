
package com.example.userservice.mapper;

import com.example.userservice.dto.UserResponse;

import com.example.userservice.model.User;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;

/**

 * MapStruct génère l'implémentation de cette interface à la compilation

 * Il copie automatiquement les champs de même nom entre User et UserResponse

 */

@Mapper(componentModel = "spring")

public interface UserMapper {

    // "status" dans User est un enum → on veut une String dans UserResponse

    @Mapping(target = "status", expression = "java(user.getStatus().name())")

    UserResponse toResponse(User user);

}


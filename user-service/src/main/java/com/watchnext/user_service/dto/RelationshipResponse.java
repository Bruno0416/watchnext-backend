package com.watchnext.user_service.dto;

// relacion del usuario de la sesion con otro perfil
public record RelationshipResponse(
    boolean following,
    boolean requested,
    boolean followsYou
) {}

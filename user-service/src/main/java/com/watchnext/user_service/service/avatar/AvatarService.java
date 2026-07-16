package com.watchnext.user_service.service.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.watchnext.user_service.exception.AvatarStorage;
import com.watchnext.user_service.exception.InvalidAvatar;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final Cloudinary cloudinary;

    private static final Set<String> ALLOWED = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );
    private static final long MAX_BYTES = 2 * 1024 * 1024;

    // ---------- gestion de archivos ----------
    public String upload(MultipartFile file, UUID profileId) {
        // 1. validar el archivo entrante
        validate(file);
        try {
            // 2. subir a cloudinary con parametros de recorte centrados en el rostro
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id",
                    "avatars/" + profileId,
                    "overwrite",
                    true,
                    "invalidate",
                    true,
                    "resource_type",
                    "image",

                    "transformation",
                    new Transformation<Transformation<?>>()
                        .width(400)
                        .height(400)
                        .crop("fill")
                        .gravity("face")
                )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new AvatarStorage("No se pudo subir el avatar", e);
        }
    }

    public void delete(UUID profileId) {
        // 1. eliminar recurso remoto mediante su id publico
        try {
            cloudinary
                .uploader()
                .destroy(
                    "avatars/" + profileId,
                    ObjectUtils.asMap("invalidate", true)
                );
        } catch (IOException e) {
            throw new AvatarStorage("No se pudo eliminar el avatar", e);
        }
    }

    // --- helper privado ---
    private void validate(MultipartFile file) {
        // 1. lanzar error si el archivo es nulo vacio o supera el tamano maximo
        if (file == null || file.isEmpty()) throw new InvalidAvatar(
            "Archivo vacío"
        );
        if (file.getSize() > MAX_BYTES) throw new InvalidAvatar(
            "La imagen supera 2 MB"
        );
        // 2. lanzar error si el formato no esta en la lista de permitidos
        if (!ALLOWED.contains(file.getContentType())) throw new InvalidAvatar(
            "Formato no permitido: " + file.getContentType()
        );
    }
}

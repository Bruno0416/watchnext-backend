package com.watchnext.list_service.repository;

import com.watchnext.common.model.ContentRef;
import com.watchnext.list_service.entity.ListItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListItemRepository extends JpaRepository<ListItem, UUID> {
    // metodo eliminar lista de items
    @Modifying
    @Query(
        "DELETE FROM ListItem i WHERE i.list.id = :listId AND i.content IN :contents"
    )
    void deleteByListIdAndContentIn(
        @Param("listId") UUID listId,
        @Param("contents") List<ContentRef> contents
    );
}

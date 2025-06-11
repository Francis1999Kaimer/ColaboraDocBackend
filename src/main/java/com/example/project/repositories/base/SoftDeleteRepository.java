package com.example.project.repositories.base;

import com.example.project.entities.base.AuditableEntity;
import com.example.project.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;



@NoRepositoryBean
public interface SoftDeleteRepository<T extends AuditableEntity, ID> extends JpaRepository<T, ID> {

    

    @Query("SELECT e FROM #{#entityName} e WHERE e.status = com.example.project.enums.EntityStatus.ACTIVE")
    List<T> findAllActive();

    

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.status = com.example.project.enums.EntityStatus.ACTIVE")
    Optional<T> findByIdAndActive(@Param("id") ID id);

    

    @Query("SELECT e FROM #{#entityName} e WHERE e.status = com.example.project.enums.EntityStatus.DELETED")
    List<T> findAllDeleted();

    

    @Query("SELECT e FROM #{#entityName} e WHERE e.status = com.example.project.enums.EntityStatus.ARCHIVED")
    List<T> findAllArchived();

    

    @Query("SELECT e FROM #{#entityName} e")
    List<T> findAllIncludingDeleted();

    

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.status = com.example.project.enums.EntityStatus.ACTIVE")
    long countActive();

    

    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.status = com.example.project.enums.EntityStatus.DELETED")
    long countDeleted();

    

    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.status = com.example.project.enums.EntityStatus.ACTIVE")
    boolean existsByIdAndActive(@Param("id") ID id);
}

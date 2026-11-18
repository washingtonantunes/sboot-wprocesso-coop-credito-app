package br.com.wti.core.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionMapping;

@Entity
@Table(name = "revinfo")
@RevisionEntity(ListenerAuditoria.class)
@Getter
@Setter
public class CustomRevEntity extends RevisionMapping {

    @Column(name = "usuario")
    private String usuario;
}

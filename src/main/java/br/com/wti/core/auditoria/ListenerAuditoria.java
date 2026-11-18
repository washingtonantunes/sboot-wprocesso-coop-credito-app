package br.com.wti.core.auditoria;

import org.hibernate.envers.RevisionListener;

public class ListenerAuditoria implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevEntity rev = (CustomRevEntity) revisionEntity;
        rev.setUsuario(AuditoriaContextHolder.getUsuario());
    }
}

package br.com.stanleydev.backendboilerplate.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantFilterAspect.class);


    @PersistenceContext
    private EntityManager entityManager;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void activateTenantFilter() {

        Session session = entityManager.unwrap(Session.class);


        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId != null) {

            log.debug("Activating tenant filter for tenantId: {}", tenantId);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        } else {

            log.debug("No tenantId in context. Tenant filter not activated.");
        }
    }
}
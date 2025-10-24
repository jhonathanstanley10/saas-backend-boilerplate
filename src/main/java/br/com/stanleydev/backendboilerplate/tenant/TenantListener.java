package br.com.stanleydev.backendboilerplate.tenant;

import jakarta.persistence.PrePersist;
import org.springframework.stereotype.Component;


@Component
public class TenantListener {

    @PrePersist
    public void onPrePersist(Object entity) {
        if (entity instanceof TenantAware) {
            String tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                throw new IllegalStateException("Tenant ID is not set in context");
            }
            ((TenantAware) entity).setTenantId(tenantId);
        }
    }
}
package br.com.stanleydev.backendboilerplate.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;


@MappedSuperclass
@Data
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = String.class)})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareBaseEntity implements TenantAware {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;


    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
package br.com.stanleydev.backendboilerplate.tenant;

import br.com.stanleydev.backendboilerplate.organization.model.Organization;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", referencedColumnName = "tenantId", insertable = false, updatable = false)
    private Organization organization;

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
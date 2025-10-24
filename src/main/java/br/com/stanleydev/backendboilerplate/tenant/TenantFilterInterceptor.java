package br.com.stanleydev.backendboilerplate.tenant;

import org.hibernate.Session;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class TenantFilterInterceptor implements HibernatePropertiesCustomizer {


    @Override
    public void customize(Map<String, Object> hibernateProperties) {


        hibernateProperties.put("hibernate.session_factory.session_opening_consumer", new Consumer<Session>() {

            @Override
            public void accept(Session session) {
                String tenantId = TenantContext.getCurrentTenant();
                if (tenantId != null) {

                    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
                }
            }
        });
    }
}

package sn.oas.facturation.shared.tenant;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Component("tenantKeyGenerator")
public class TenantKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        String tenant = TenantContext.getCurrentTenant();
        return tenant + "_" + method.getName() + "_" + StringUtils.arrayToDelimitedString(params, "_");
    }
}

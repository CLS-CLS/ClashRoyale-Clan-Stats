package org.lytsiware.clash.service.integration.proxy;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class QuotaCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String proxyName = context.getEnvironment().getProperty("PROXY_NAME");
        return "QUOTAGUARDSTATIC".equals(proxyName);
    }
}

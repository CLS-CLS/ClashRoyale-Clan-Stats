package org.lytsiware.clash.service.integration.clashapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.Proxy;

@Component
@Conditional(value = QuotaCondition.class)
public class QuotaBearerHolder implements ProxyAndBearerHolder {

    @Value("${QUOTAGUARDSTATIC_BEARER}")
    private String quotaguardstaticBearer;

    @Value("${QUOTAGUARDSTATIC_URL}")
    private String quotaguardstaticUrl;

    private Proxy proxy;

    @PostConstruct
    protected void initProxy() {
        proxy = createProxy(quotaguardstaticUrl);
    }

    @Override
    public String getBearer() {
        return quotaguardstaticBearer;
    }

    @Override
    public Proxy getProxy() {
        return proxy;
    }
}

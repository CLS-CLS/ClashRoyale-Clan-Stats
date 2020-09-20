package org.lytsiware.clash.core.service.integration.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.net.Proxy;


@Component
@ConditionalOnMissingBean({FixieBearerHolder.class, QuotaBearerHolder.class})
public class NoProxyBearerHolder implements ProxyAndBearerHolder {

    @Value("${NO_PROXY_BEARER:}")
    String bearer;

    @Override
    public String getBearer() {
        return bearer;
    }

    @Override
    public Proxy getProxy() {
        return Proxy.NO_PROXY;
    }
}

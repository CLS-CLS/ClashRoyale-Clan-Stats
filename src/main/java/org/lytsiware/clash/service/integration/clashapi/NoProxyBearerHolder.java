package org.lytsiware.clash.service.integration.clashapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.net.Proxy;


@Component
@ConditionalOnMissingBean({FixieBearerHolder.class, QuotaBearerHolder.class})
public class NoProxyBearerHolder implements ProxyAndBearerHolder {
    @Override
    public String getBearer() {
        return "";
    }

    @Override
    public Proxy getProxy() {
        return Proxy.NO_PROXY;
    }
}

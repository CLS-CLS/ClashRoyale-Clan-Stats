package org.lytsiware.clash.core.service.integration.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.Proxy;


@Component
@Conditional(FixieCondition.class)
public class FixieBearerHolder implements ProxyAndBearerHolder {

    @Value("${FIXIE_URL}")
    private String fixieUrl;

    @Value("${FIXIE_BEARER}")
    private String fixieBearer;

    private Proxy proxy;

    @PostConstruct
    public void initProxy() {
        proxy = createProxy(fixieUrl);
    }

    @Override
    public String getBearer() {
        return fixieBearer;
    }

    public Proxy getProxy() {
        return proxy;
    }

}

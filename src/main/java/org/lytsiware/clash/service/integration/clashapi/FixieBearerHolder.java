package org.lytsiware.clash.service.integration.clashapi;

import org.springframework.stereotype.Component;

import java.net.Proxy;


@Component
public class FixieBearerHolder implements ProxyAndBearerHolder {


    @Override
    public String getBearer() {
        return "";
    }

    @Override
    public Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

}

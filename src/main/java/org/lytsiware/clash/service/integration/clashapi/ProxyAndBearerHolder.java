package org.lytsiware.clash.service.integration.clashapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public interface ProxyAndBearerHolder {

    Logger log = LoggerFactory.getLogger(ProxyAndBearerHolder.class);

    String getBearer();

    Proxy getProxy();


    default Proxy createProxy(String url) {
        String[] urlValues = url.split("[/(:\\/@)/]+");
        if (urlValues.length < 4) {
            log.warn("proxy url {} is not correct - Defaulting to NO_PROXY", url);
            return java.net.Proxy.NO_PROXY;
        }
        String user = urlValues[1];
        String password = urlValues[2];
        String host = urlValues[3];
        int port = Integer.parseInt(urlValues[4]);
        log.info("Creating proxy with host {} : ", host);
        java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));

        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(user, password.toCharArray()));
            }
        };
        Authenticator.setDefault(authenticator);
        return proxy;
    }
}

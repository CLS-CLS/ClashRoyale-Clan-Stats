package org.lytsiware.clash.service.integration.clashapi;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Service
public class ProxyService {

    //    @Value("${env.FIXIE_URL")
    private String fixieUrl = "http://test:test@olympic.usefixie.com:80";

    private Proxy proxy;

    public ProxyService() {

    }

    public ProxyService(String fixieUrl) {
        this.fixieUrl = fixieUrl;
    }


    @PostConstruct
    public void initProxy() {
        String[] fixieValues = fixieUrl.split("[/(:\\/@)/]+");
        String fixieUser = fixieValues[1];
        String fixiePassword = fixieValues[2];
        String fixieHost = fixieValues[3];
        int fixiePort = Integer.parseInt(fixieValues[4]);
        byte[] addr = new InetSocketAddress(fixieHost, fixiePort).getAddress().getAddress();
        String ipAddr = "";
        for (int i = 0; i < addr.length; i++) {
            if (i > 0) {
                ipAddr += ".";
            }
            ipAddr += addr[i] & 0xFF;
        }

        System.out.println("IP Address: " + ipAddr);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort));

        Authenticator authenticator = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication(fixieUser, fixiePassword.toCharArray()));
            }
        };
        Authenticator.setDefault(authenticator);
        this.proxy = proxy;
    }


    public Proxy getProxy() {
        return proxy;

    }
}

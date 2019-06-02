package org.lytsiware.clash.runners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

//@Component
@Slf4j
public class EnvVarsCheck implements CommandLineRunner {

    @Value("${BEARER}")
    String bearer;

    @Value("${FIXIE_URL}")
    private String fixieUrl;

    @Override
    public void run(String... args) throws Exception {
        log.info("BEARER IS {} ", bearer);
        log.info("Fixie URL {} ", fixieUrl);
        if (bearer == null || fixieUrl == null) {
            System.exit(0);
        }
    }
}

package org.lytsiware.clash.core.service.integration;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface SiteQualifier {

    Name value();

    enum Name {
        STATS_ROYALE,
        STATS_ROYALE_WAR,
        DECK_SHOP
    }
}
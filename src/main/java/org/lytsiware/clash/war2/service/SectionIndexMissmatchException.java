package org.lytsiware.clash.war2.service;

public class SectionIndexMissmatchException extends RuntimeException {

    public SectionIndexMissmatchException() {

    }

    public SectionIndexMissmatchException(int entitySection, int dtoSection) {
        super(String.format("Entity section %s does not match dto's %s", entitySection, dtoSection));
    }
}

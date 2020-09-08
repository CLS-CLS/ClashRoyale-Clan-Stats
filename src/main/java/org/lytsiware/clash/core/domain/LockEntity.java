package org.lytsiware.clash.core.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "LOCK")
public class LockEntity {

    public static final Long PRIMARY_KEY = 1L;

    @Id
    private Long id;

    private boolean locked;

    public LockEntity() {
        id = PRIMARY_KEY;
    }
}

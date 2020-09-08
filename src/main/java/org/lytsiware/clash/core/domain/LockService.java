package org.lytsiware.clash.core.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
public class LockService {

    @Autowired
    EntityManager entityManager;


    public boolean isLocked() {
        return entityManager.find(LockEntity.class, LockEntity.PRIMARY_KEY).isLocked();
    }

    /**
     * updates the lock table with locked=true. The table is updated only if the value of column 'locked' is false
     *
     * @return true if the column is updated, false otherwise
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean lock() {
        LockEntity lockEntity = entityManager.find(LockEntity.class, LockEntity.PRIMARY_KEY);
        if (lockEntity.isLocked()) {
            return false;
        }

        lockEntity.setLocked(true);
        entityManager.flush();
        return true;
    }

    /**
     * sets the value of the column locked to false
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlock() {
        entityManager.find(LockEntity.class, LockEntity.PRIMARY_KEY).setLocked(false);
        entityManager.flush();
    }

}

package com.tour.registration.store;

import com.tour.registration.model.StoredRegistration;

import java.util.ArrayList;
import java.util.List;

/** 本機試跑用：資料只放在記憶體，伺服器重開就消失。 */
public class InMemoryRegistrationStore implements RegistrationStore {

    private final List<StoredRegistration> rows = new ArrayList<>();

    @Override
    public synchronized List<StoredRegistration> findAll() {
        return List.copyOf(rows);
    }

    @Override
    public synchronized void insert(StoredRegistration registration) {
        rows.add(registration);
    }

    @Override
    public synchronized void update(StoredRegistration registration) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id().equals(registration.id())) {
                rows.set(i, registration);
                return;
            }
        }
        throw new IllegalStateException("找不到報名 " + registration.id());
    }
}

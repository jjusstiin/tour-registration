package com.tour.registration.store;

import com.tour.registration.model.StoredRegistration;

import java.util.List;
import java.util.Optional;

/** 報名資料存放處。正式環境是 Google 試算表，本機沒設金鑰時是記憶體。 */
public interface RegistrationStore {

    List<StoredRegistration> findAll();

    default Optional<StoredRegistration> findById(String id) {
        return findAll().stream().filter(r -> r.id().equals(id)).findFirst();
    }

    void insert(StoredRegistration registration);

    void update(StoredRegistration registration);
}

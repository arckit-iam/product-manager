// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.service;

import java.time.LocalDate;
import java.util.List;

import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.Ressource;

public interface ProfileApplicaManager {
    List<Product> getAllProducts();
    Product getProduct(Long id);
    Product getProduct(String productName);
    Product createProduct(Product product) throws UniqueProductNameConstraintViolationException;
    void deleteProduct(Long id);
    void deleteProduct(String productName);
    Product updateProduct(Long id, Product domain);
    Product updateProduct(String productName, Product domain);
    Product addResourceToProduct(String productName, Ressource resource);
    Product addProfileToProduct(String productName, Profile profile);

    /** Supprime un profil : annule ses ordres en attente et, s'il existe déjà, ordonne sa suppression. */
    Product deleteProfileToProduct(String productName, String profileName);

    /** Change les dates de début et de fin d'un profil, en remplaçant ses ordres en attente. */
    Product updateProfileDates(String productName, String profileName, LocalDate startDate, LocalDate endDate);

    /**
     * Annule ce qui est encore à venir pour un profil : sa création (le profil est alors supprimé) ou,
     * s'il existe déjà, sa suppression planifiée.
     */
    Product cancelProfilePending(String productName, String profileName);
}
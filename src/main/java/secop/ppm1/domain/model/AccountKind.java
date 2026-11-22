// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

/**
 * Nature d'un compte. Product-manager ne gère que les comptes {@link #NON_USER} (rattachés à un produit) ;
 * les comptes {@link #USER} (rattachés à une identité humaine) relèvent de l'identity-manager.
 */
public enum AccountKind {
    NON_USER,
    USER
}

// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.port;

import java.util.UUID;

import secop.ppm1.domain.model.Profile;

public interface JobPublisher {
    UUID publishCreateEntitlement(String productName, Profile profile);
    UUID publishUpdateEntitlement(String productName, Profile profile);
    UUID publishDeleteEntitlement(String productName, Profile profile);
}

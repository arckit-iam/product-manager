// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class ProfileDto {
    String name;
    String description;
    String scope;
    LocalDate startDate;
    LocalDate endDate;
    UUID orderId;
    UUID endOrderId;
}

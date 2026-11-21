// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.util.UUID;

import lombok.Data;

@Data
public class Profile {
    String product;
    String name;
    String description;
    String scope;
    UUID jobId;
}

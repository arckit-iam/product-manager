// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import lombok.Data;

@Data
public class Ressource {
    String name;
    TypeRessource typeRessource;
    Product product;

    public enum Criticite {
        LOW,
        MEDIUM,
        HIGH
    }
}

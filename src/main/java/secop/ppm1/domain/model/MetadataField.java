// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

/** Champ de métadonnées déclaré par un type de compte. */
public record MetadataField(String name, MetadataType type, boolean required) {

    public MetadataField {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Metadata field name is required");
        }
        type = type == null ? MetadataType.STRING : type;
    }
}

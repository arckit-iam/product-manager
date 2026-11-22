// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

/** Types de valeur acceptés pour un champ de métadonnées. */
public enum MetadataType {
    STRING,
    NUMBER,
    BOOLEAN;

    /** @return vrai si la valeur est du type attendu. */
    public boolean accepts(Object value) {
        return switch (this) {
            case STRING -> value instanceof CharSequence;
            case NUMBER -> value instanceof Number;
            case BOOLEAN -> value instanceof Boolean;
        };
    }
}

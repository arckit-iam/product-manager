// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import secop.ppm1.domain.exception.InvalidAccountException;

/**
 * Type de compte, défini par configuration : il porte l'expression régulière qui reconnaît ses
 * identifiants (les groupes nommés {@code product} ou {@code identity} servent à la corrélation) et
 * la liste des champs de métadonnées autorisés.
 */
public record AccountType(String name, AccountKind kind, Pattern pattern, List<MetadataField> metadata) {

    public AccountType {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Account type name is required");
        }
        if (kind == null) {
            throw new IllegalArgumentException("Account type '" + name + "' needs a kind");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("Account type '" + name + "' needs a pattern");
        }
        metadata = metadata == null ? List.of() : List.copyOf(metadata);
    }

    /** Reconnaît l'identifiant en entier (la regex doit couvrir tout l'identifiant). */
    public Optional<AccountMatch> match(String identifier) {
        if (identifier == null) {
            return Optional.empty();
        }
        Matcher matcher = pattern.matcher(identifier);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        Map<String, String> captures = new HashMap<>();
        pattern.namedGroups().keySet().forEach(group -> {
            String value = matcher.group(group);
            if (value != null && !value.isBlank()) {
                captures.put(group, value);
            }
        });
        return Optional.of(new AccountMatch(this, captures));
    }

    /**
     * Vérifie les métadonnées : aucun champ inconnu, champs obligatoires présents, valeurs du bon type.
     *
     * @throws InvalidAccountException si les métadonnées ne respectent pas le type
     */
    public void validateMetadata(Map<String, Object> values) {
        Map<String, Object> actual = values == null ? Map.of() : values;
        for (String key : actual.keySet()) {
            if (metadata.stream().noneMatch(f -> f.name().equals(key))) {
                throw new InvalidAccountException("Unknown metadata '" + key + "' for type " + name);
            }
        }
        for (MetadataField field : metadata) {
            Object value = actual.get(field.name());
            if (value == null) {
                if (field.required()) {
                    throw new InvalidAccountException("Metadata '" + field.name() + "' is required for type " + name);
                }
            } else if (!field.type().accepts(value)) {
                throw new InvalidAccountException(
                        "Metadata '" + field.name() + "' must be of type " + field.type());
            }
        }
    }
}

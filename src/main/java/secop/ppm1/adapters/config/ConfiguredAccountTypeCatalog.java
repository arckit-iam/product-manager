// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.config;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import secop.ppm1.domain.model.AccountType;
import secop.ppm1.domain.model.MetadataField;
import secop.ppm1.domain.port.AccountTypeCatalog;

/**
 * Catalogue construit au démarrage depuis la configuration : une regex invalide ou un type en double
 * empêche le démarrage plutôt que de fausser silencieusement la corrélation.
 */
@Component
@EnableConfigurationProperties(AccountTypeProperties.class)
public class ConfiguredAccountTypeCatalog implements AccountTypeCatalog {

    private final List<AccountType> types;

    public ConfiguredAccountTypeCatalog(AccountTypeProperties properties) {
        Set<String> names = new HashSet<>();
        this.types = properties.getTypes().stream().map(type -> {
            if (!names.add(type.getName())) {
                throw new IllegalArgumentException("Duplicate account type: " + type.getName());
            }
            return toDomain(type);
        }).toList();
    }

    private static AccountType toDomain(AccountTypeProperties.Type type) {
        if (type.getPattern() == null || type.getPattern().isBlank()) {
            throw new IllegalArgumentException("Account type '" + type.getName() + "' needs a pattern");
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(type.getPattern());
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                    "Invalid pattern for account type '" + type.getName() + "': " + e.getDescription(), e);
        }
        List<MetadataField> fields = type.getMetadata().stream()
                .map(f -> new MetadataField(f.getName(), f.getType(), f.isRequired()))
                .toList();
        return new AccountType(type.getName(), type.getKind(), pattern, fields);
    }

    @Override
    public List<AccountType> findAll() {
        return types;
    }

    @Override
    public Optional<AccountType> findByName(String name) {
        return types.stream().filter(t -> t.name().equals(name)).findFirst();
    }
}

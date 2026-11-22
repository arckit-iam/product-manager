// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.MetadataType;

/**
 * Types de compte définis par le déploiement (HelmRelease, variables d'environnement) sous
 * {@code arc.accounts.types}. Exemple en variables d'environnement :
 * <pre>
 * ARC_ACCOUNTS_TYPES_0_NAME=service-account
 * ARC_ACCOUNTS_TYPES_0_KIND=NON_USER
 * ARC_ACCOUNTS_TYPES_0_PATTERN=svc-(?&lt;product&gt;[a-z0-9]+)-[a-z0-9]+
 * ARC_ACCOUNTS_TYPES_0_METADATA_0_NAME=owner
 * ARC_ACCOUNTS_TYPES_0_METADATA_0_TYPE=STRING
 * ARC_ACCOUNTS_TYPES_0_METADATA_0_REQUIRED=true
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "arc.accounts")
public class AccountTypeProperties {

    private List<Type> types = new ArrayList<>();

    @Data
    public static class Type {
        private String name;
        private AccountKind kind;
        private String pattern;
        private List<Field> metadata = new ArrayList<>();
    }

    @Data
    public static class Field {
        private String name;
        private MetadataType type = MetadataType.STRING;
        private boolean required;
    }
}

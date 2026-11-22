// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import secop.ppm1.domain.model.AccountKind;
import secop.ppm1.domain.model.AccountMatch;
import secop.ppm1.domain.model.MetadataType;

class ConfiguredAccountTypeCatalogTest {

    private static AccountTypeProperties bind(java.util.Map<String, String> source) {
        return new Binder(new MapConfigurationPropertySource(source))
                .bind("arc.accounts", AccountTypeProperties.class).orElseGet(AccountTypeProperties::new);
    }

    private static ConfiguredAccountTypeCatalog catalog() {
        return new ConfiguredAccountTypeCatalog(bind(java.util.Map.of(
                "arc.accounts.types[0].name", "service-account",
                "arc.accounts.types[0].kind", "NON_USER",
                "arc.accounts.types[0].pattern", "svc-(?<product>[a-z0-9]+)-[a-z]+",
                "arc.accounts.types[0].metadata[0].name", "owner",
                "arc.accounts.types[0].metadata[0].required", "true",
                "arc.accounts.types[0].metadata[1].name", "rotationDays",
                "arc.accounts.types[0].metadata[1].type", "NUMBER",
                "arc.accounts.types[1].name", "employee",
                "arc.accounts.types[1].kind", "USER",
                "arc.accounts.types[1].pattern", "(?<identity>[a-z]+)")));
    }

    @Test
    void buildsTypesFromConfiguration() {
        ConfiguredAccountTypeCatalog catalog = catalog();

        assertEquals(2, catalog.findAll().size());
        var type = catalog.findByName("service-account").orElseThrow();
        assertEquals(AccountKind.NON_USER, type.kind());
        assertEquals(MetadataType.STRING, type.metadata().get(0).type());
        assertTrue(type.metadata().get(0).required());
        assertEquals(MetadataType.NUMBER, type.metadata().get(1).type());
        assertEquals(Optional.empty(), catalog.findByName("unknown"));
    }

    @Test
    void classifiesByKind() {
        ConfiguredAccountTypeCatalog catalog = catalog();

        Optional<AccountMatch> service = catalog.classify("svc-compta-batch", AccountKind.NON_USER);
        assertEquals("service-account", service.orElseThrow().type().name());
        assertEquals(Optional.of("compta"), service.get().product());
        assertEquals(Optional.empty(), catalog.classify("alice", AccountKind.NON_USER));
        assertEquals(Optional.of("alice"), catalog.classify("alice", AccountKind.USER).orElseThrow().identity());
    }

    @Test
    void noTypeConfiguredMeansEmptyCatalog() {
        assertEquals(List.of(), new ConfiguredAccountTypeCatalog(bind(java.util.Map.of())).findAll());
    }

    @Test
    void rejectsInvalidPattern() {
        AccountTypeProperties props = bind(java.util.Map.of(
                "arc.accounts.types[0].name", "bad",
                "arc.accounts.types[0].kind", "NON_USER",
                "arc.accounts.types[0].pattern", "svc-(unclosed"));

        assertThrows(IllegalArgumentException.class, () -> new ConfiguredAccountTypeCatalog(props));
    }

    @Test
    void rejectsMissingPattern() {
        AccountTypeProperties props = bind(java.util.Map.of(
                "arc.accounts.types[0].name", "bad",
                "arc.accounts.types[0].kind", "NON_USER"));

        assertThrows(IllegalArgumentException.class, () -> new ConfiguredAccountTypeCatalog(props));
    }

    @Test
    void rejectsDuplicateNames() {
        AccountTypeProperties props = bind(java.util.Map.of(
                "arc.accounts.types[0].name", "dup",
                "arc.accounts.types[0].kind", "NON_USER",
                "arc.accounts.types[0].pattern", "a",
                "arc.accounts.types[1].name", "dup",
                "arc.accounts.types[1].kind", "USER",
                "arc.accounts.types[1].pattern", "b"));

        assertThrows(IllegalArgumentException.class, () -> new ConfiguredAccountTypeCatalog(props));
    }
}

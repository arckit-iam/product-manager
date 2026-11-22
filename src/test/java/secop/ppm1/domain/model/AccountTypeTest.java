// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import secop.ppm1.domain.exception.InvalidAccountException;

class AccountTypeTest {

    private final AccountType type = new AccountType("service-account", AccountKind.NON_USER,
            Pattern.compile("svc-(?<product>[a-z0-9]+)-(?<role>[a-z]+)"),
            List.of(new MetadataField("owner", MetadataType.STRING, true),
                    new MetadataField("rotationDays", MetadataType.NUMBER, false),
                    new MetadataField("interactive", MetadataType.BOOLEAN, false)));

    @Test
    void capturesNamedGroups() {
        Optional<AccountMatch> match = type.match("svc-compta-batch");

        assertTrue(match.isPresent());
        assertEquals(Optional.of("compta"), match.get().product());
        assertEquals(Optional.empty(), match.get().identity());
        assertEquals("batch", match.get().captures().get("role"));
    }

    @Test
    void patternMustCoverTheWholeIdentifier() {
        assertFalse(type.match("xsvc-compta-batch").isPresent());
        assertFalse(type.match("svc-compta-batch2").isPresent());
        assertFalse(type.match(null).isPresent());
    }

    @Test
    void identityGroupIsExposed() {
        AccountType user = new AccountType("employee", AccountKind.USER,
                Pattern.compile("(?<identity>[a-z]+)(-adm)?"), null);

        assertEquals(Optional.of("alice"), user.match("alice-adm").orElseThrow().identity());
        assertEquals(List.of(), user.metadata());
    }

    @Test
    void ignoresBlankCaptures() {
        AccountType optional = new AccountType("t", AccountKind.NON_USER,
                Pattern.compile("a(?<product>x)?"), List.of());

        assertEquals(Optional.empty(), optional.match("a").orElseThrow().product());
    }

    @Test
    void acceptsValidMetadata() {
        type.validateMetadata(Map.of("owner", "alice", "rotationDays", 30, "interactive", false));
        type.validateMetadata(Map.of("owner", "alice"));
    }

    @Test
    void rejectsMissingRequiredMetadata() {
        assertThrows(InvalidAccountException.class, () -> type.validateMetadata(Map.of()));
        assertThrows(InvalidAccountException.class, () -> type.validateMetadata(null));
    }

    @Test
    void rejectsUnknownMetadata() {
        assertThrows(InvalidAccountException.class,
                () -> type.validateMetadata(Map.of("owner", "alice", "other", "x")));
    }

    @Test
    void rejectsMetadataOfTheWrongType() {
        assertThrows(InvalidAccountException.class,
                () -> type.validateMetadata(Map.of("owner", 12)));
        assertThrows(InvalidAccountException.class,
                () -> type.validateMetadata(Map.of("owner", "a", "rotationDays", "30")));
        assertThrows(InvalidAccountException.class,
                () -> type.validateMetadata(Map.of("owner", "a", "interactive", "yes")));
    }

    @Test
    void rejectsIncompleteDefinitions() {
        Pattern p = Pattern.compile(".*");
        assertThrows(IllegalArgumentException.class, () -> new AccountType(" ", AccountKind.USER, p, null));
        assertThrows(IllegalArgumentException.class, () -> new AccountType("t", null, p, null));
        assertThrows(IllegalArgumentException.class, () -> new AccountType("t", AccountKind.USER, null, null));
        assertThrows(IllegalArgumentException.class, () -> new MetadataField("", MetadataType.STRING, false));
    }

    @Test
    void metadataTypeDefaultsToString() {
        assertEquals(MetadataType.STRING, new MetadataField("f", null, false).type());
    }
}

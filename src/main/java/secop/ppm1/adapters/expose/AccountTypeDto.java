// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.util.List;

public record AccountTypeDto(String name, String kind, String pattern, List<MetadataFieldDto> metadata) {

    public record MetadataFieldDto(String name, String type, boolean required) {
    }
}

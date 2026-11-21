// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.domain.model;

import java.util.List;

import lombok.Data;

@Data
public class Product {
    
    private String name;

    private String description;

    private List<Profile> profiles;

    private List<Ressource> ressources;

}

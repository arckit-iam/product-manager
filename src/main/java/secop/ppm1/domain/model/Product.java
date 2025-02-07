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

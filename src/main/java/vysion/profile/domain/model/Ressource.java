package vysion.profile.domain.model;

import lombok.Data;

@Data
public class Ressource {
    String name;
    TypeRessource typeRessource;
    Product product;

    public enum Criticite {
        LOW,
        MEDIUM,
        HIGH
    }
}

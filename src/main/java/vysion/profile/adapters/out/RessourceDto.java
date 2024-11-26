package vysion.profile.adapters.out;

import vysion.profile.adapters.in.ProductEntity;
import vysion.profile.adapters.in.RessourceTypeEntity;

public class RessourceDto {
    String name;
    RessourceTypeEntity typeRessource;
    ProductEntity product;

    public enum Criticite {
        LOW,
        MEDIUM,
        HIGH
    }
}

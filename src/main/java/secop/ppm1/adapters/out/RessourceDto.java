package secop.ppm1.adapters.out;

import secop.ppm1.adapters.in.ProductEntity;
import secop.ppm1.adapters.in.RessourceTypeEntity;

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

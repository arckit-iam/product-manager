package secop.ppm1.adapters.expose;

import secop.ppm1.adapters.provide.ProductEntity;
import secop.ppm1.adapters.provide.RessourceTypeEntity;

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

package vysion.profile.adapters.out;

import java.util.List;

import lombok.Data;

@Data
public class DtoProduct {
    private String identifiant;
    private List<DtoProfile> profilesByProduct;

}

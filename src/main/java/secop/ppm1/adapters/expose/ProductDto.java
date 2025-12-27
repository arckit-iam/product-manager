package secop.ppm1.adapters.expose;

import java.util.List;

import lombok.Data;

@Data
public class ProductDto {

    private String name;

    private String description;

    private List<ProfileDto> profiles;

}

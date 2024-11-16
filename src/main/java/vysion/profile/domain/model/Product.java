package vysion.profile.domain.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Product {
    
    @Id
    private String identifiant;

}

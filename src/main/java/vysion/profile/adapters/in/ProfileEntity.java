package vysion.profile.adapters.in;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import vysion.profile.domain.model.Product;

@Entity
@Data
@Table(name = "profile")

public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
  
    private String name;
}

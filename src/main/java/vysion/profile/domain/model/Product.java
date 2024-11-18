package vysion.profile.domain.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import vysion.profile.adapters.in.ProfileEntity;
import vysion.profile.adapters.in.RessourceEntity;

@Data
public class Product {
    
    @Id
    private String name;

    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfileEntity> profiles;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RessourceEntity> ressources;

}

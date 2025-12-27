package secop.ppm1.adapters.provide;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "resource_type")
public class RessourceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "resourceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RessourceEntity> ressources;

    @OneToMany(mappedBy = "resourceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PermissionTypeEntity> permissionTypes;
}

package secop.ppm1.adapters.provide;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "permission_type")
public class PermissionTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "resource_type_id", nullable = false)
    private RessourceTypeEntity resourceType;

    @OneToMany(mappedBy = "permissionType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PermissionEntity> permissions;
}

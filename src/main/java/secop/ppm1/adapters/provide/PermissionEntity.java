package secop.ppm1.adapters.provide;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "permission")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @ManyToOne
    @JoinColumn(name = "ressource_id", nullable = false)
    private RessourceEntity ressource;

    @ManyToOne
    @JoinColumn(name = "permission_type_id", nullable = false)
    private PermissionTypeEntity permissionType;
}

package vysion.profile.adapters.in;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "permission")
public class PermissionEntity {
    TypePermissionEntity type;
    String Description;
}

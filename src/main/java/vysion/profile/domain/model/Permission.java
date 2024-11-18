package vysion.profile.domain.model;

import lombok.Data;

@Data
public class Permission {

    TypePermission type;
    Profile profile;

}

package vysion.profile.adapters.in;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vysion.profile.domain.model.Profile;

@Mapper(componentModel = "spring")
public interface ProfileEntityMapper {
    Profile toDomain(ProfileEntity entity);
    @Mapping(target = "id", ignore = true) // Ignorer le champ id
    ProfileEntity toEntity(Profile domain);
     // Mapper une liste de ProfileEntity vers une liste de Profile
    List<Profile> toDomainList(List<ProfileEntity> entities);

    // Mapper une liste de Profile vers une liste de ProfileEntity
    List<ProfileEntity> toEntityList(List<Profile> profiles);
}

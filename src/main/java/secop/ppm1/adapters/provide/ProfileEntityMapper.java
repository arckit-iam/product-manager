package secop.ppm1.adapters.provide;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import secop.ppm1.domain.model.Profile;

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

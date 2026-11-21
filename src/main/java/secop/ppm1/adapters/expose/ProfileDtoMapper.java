// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;

import java.util.List;

import org.mapstruct.Mapper;

import secop.ppm1.domain.model.Profile;

@Mapper(componentModel = "spring")
public interface ProfileDtoMapper {
    ProfileDto toDto(Profile domain);
    Profile toDomain(ProfileDto dto);
    // Mapper une liste de ProfileDto vers une liste de Profile
    List<Profile> toDomainList(List<ProfileDto> dtos);

    // Mapper une liste de Profile vers une liste de ProfileDto
    List<ProfileDto> toDtoList(List<Profile> Profiles);
}

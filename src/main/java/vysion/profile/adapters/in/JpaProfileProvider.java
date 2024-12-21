package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import vysion.profile.domain.model.Profile;
import vysion.profile.domain.port.ProfileProvider;

@Repository
public class JpaProfileProvider implements ProfileProvider{

    private final JpaProfileRepository profileRepository;
    private final ProfileEntityMapper profileEntityMapper;


    @Autowired
    public JpaProfileProvider(JpaProfileRepository profileRepository,ProfileEntityMapper profileEntityMapper) {
        this.profileRepository = profileRepository;
        this.profileEntityMapper = profileEntityMapper;
    }
    @Override
    public Profile save(Profile profile,Long productId) {
        return profileEntityMapper.toDomain(profileRepository.saveAndFlush(profileEntityMapper.toEntity(profile).addProductId(productId)));
    }

    @Override
    public Profile getProfileById(Long id) {
        return profileEntityMapper.toDomain(profileRepository.findById(id).orElseThrow(()-> new NotFoundException("The Profile does not exist")));
    }

    @Override
    public List<Profile> getProfilesByProduct(Long productId) {
        return profileEntityMapper.toDomainList(profileRepository.findAllByProductId(productId));
    }
    
}

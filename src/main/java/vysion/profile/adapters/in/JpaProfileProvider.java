package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import vysion.profile.domain.model.Product;
import vysion.profile.domain.model.Profile;
import vysion.profile.domain.port.ProfileProvider;

@Repository
public class JpaProfileProvider implements ProfileProvider{

    private final JpaProfileRepository profileRepository;
    private final JpaProductRepository productRepository;
    private final ProfileEntityMapper profileEntityMapper;
    private final ProductEntityMapper productEntityMapper;


    @Autowired
    public JpaProfileProvider(JpaProfileRepository profileRepository,JpaProductRepository productRepository,ProfileEntityMapper profileEntityMapper, ProductEntityMapper productEntityMapper) {
        this.profileRepository = profileRepository;
        this.productRepository = productRepository;
        this.profileEntityMapper = profileEntityMapper;
        this.productEntityMapper = productEntityMapper;
    }
    @Override
    public Profile save(Profile profile,Long productId) {
        return profileEntityMapper.toDomain(profileRepository.saveAndFlush(profileEntityMapper.toEntity(profile).addId(productId)));
    }

    @Override
    public Profile getProfileById(Long id) {
        return profileEntityMapper.toDomain(profileRepository.findById(id).orElseThrow(()-> new NotFoundException("The Profile does not exist")));
    }

    @Override
    public List<Profile> getProfilesByProduct(Product product) {
        if(!productRepository.existsByName(product.getName())){
            throw new NotFoundException("The product does not exist");
        }
        return profileEntityMapper.toDomainList(profileRepository.findAllByProduct(productEntityMapper.toEntity(product)));
    }
    
}

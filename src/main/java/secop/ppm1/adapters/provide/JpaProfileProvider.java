// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.port.ProfileProvider;

@Repository
public class JpaProfileProvider implements ProfileProvider{

    private final JpaProfileRepository profileRepository;
    private final ProfileEntityMapper profileEntityMapper;
    private final JpaProductRepository productRepository;


    @Autowired
    public JpaProfileProvider(JpaProfileRepository profileRepository, ProfileEntityMapper profileEntityMapper, JpaProductRepository productRepository) {
        this.profileRepository = profileRepository;
        this.profileEntityMapper = profileEntityMapper;
        this.productRepository = productRepository;
    }
    @Override
    public Profile save(Profile profile,Long productId) {
        return profileEntityMapper.toDomain(profileRepository.saveAndFlush(profileEntityMapper.toEntity(profile).addProductId(productId)));
    }
    @Override
    public Profile save(Profile profile,String productName) {
        ProfileEntity profileEntity = profileEntityMapper.toEntity(profile);
        Long productId = productRepository.findByName(productName).getId();
        profileEntity.setProductId(productId);
        ProfileEntity existing = profileRepository.findOneByNameAndProductId(profile.getName(), productId);
        if (existing != null) {
            profileEntity.setId(existing.getId());
        }
        return profileEntityMapper.toDomain(profileRepository.saveAndFlush(profileEntity));
    }



    @Override
    public void deleteProfile(String productName, String profileName) {
        ProductEntity product = productRepository.findByName(productName);
        if (product == null) {
            return;
        }
        ProfileEntity entity = profileRepository.findOneByNameAndProductId(profileName, product.getId());
        if (entity != null) {
            profileRepository.delete(entity);
        }
    }
    @Override
    public Profile getProfileByNameAndProduct(String profileName, String productName) {
        return profileEntityMapper.toDomain(profileRepository.findOneByNameAndProductId(profileName, productRepository.findByName(productName).getId()));
    }
    @Override
    public List<Profile> getProfilesByProduct(String productName) {
        return profileEntityMapper.toDomainList(profileRepository.findAllByProductId(productRepository.findByName(productName).getId()));   
    }
    
}

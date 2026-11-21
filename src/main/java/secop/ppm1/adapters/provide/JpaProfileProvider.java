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
        profileEntity.setProductId(productRepository.findByName(productName).getId());
        return profileEntityMapper.toDomain(profileRepository.saveAndFlush(profileEntity));
    }



    @Override
    public void deleteProfile(Profile profile) {
       
        profileRepository.delete(profileEntityMapper.toEntity(profile));
                
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

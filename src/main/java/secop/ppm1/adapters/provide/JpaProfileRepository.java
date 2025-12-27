package secop.ppm1.adapters.provide;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProfileRepository extends JpaRepository<ProfileEntity, Long>{
    
    List<ProfileEntity> findAllByProductId(Long productId);
    ProfileEntity findOneByNameAndProductId(String profileName, Long productId);

}

package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProfileRepository extends JpaRepository<ProfileEntity, Long>{
    List<ProfileEntity> findAllByProduct(ProductEntity productEntity);
}

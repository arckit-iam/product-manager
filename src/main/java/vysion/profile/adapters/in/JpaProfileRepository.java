package vysion.profile.adapters.in;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProfileRepository extends JpaRepository<ProfileEntity, Long>{
    
}

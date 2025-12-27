package secop.ppm1.adapters.provide;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductRepository extends JpaRepository<ProductEntity, Long>{

    boolean existsByName(String name);
    ProductEntity findByName(String name);    
    void deleteByName(String name);
    @Query("SELECT p.id FROM ProductEntity p WHERE p.name = :productName")
    Long findIdByProductName(@Param("productName") String productName);    
}

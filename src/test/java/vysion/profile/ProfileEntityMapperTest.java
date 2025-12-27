package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import secop.ppm1.adapters.provide.ProfileEntity;
import secop.ppm1.adapters.provide.ProfileEntityMapper;
import secop.ppm1.domain.model.Profile;

public class ProfileEntityMapperTest {

    private ProfileEntityMapper profileEntityMapper;
    private Profile profile;
    private ProfileEntity profileEntity;

    @BeforeEach
    public void setUp() {
        profileEntityMapper = Mappers.getMapper(ProfileEntityMapper.class);
        
        profile = new Profile();
        profile.setName("Test Profile");
        profile.setProduct("Test Product");
        profile.setDescription("Test Description");

        profileEntity = new ProfileEntity();
        profileEntity.setId(1L);
        profileEntity.setName("Test Profile");
        profileEntity.setProductId(1L);
        profileEntity.setDescription("Test Description");
    }

    @Test
    public void testToDomain() {
        Profile result = profileEntityMapper.toDomain(profileEntity);

        assertNotNull(result);
        assertEquals("Test Profile", result.getName());
        assertEquals("Test Description", result.getDescription());
        // Note: Le mapping de productId vers product n'est pas automatique avec MapStruct
        // Il faudrait une méthode de mapping personnalisée pour cela
    }

    @Test
    public void testToDomain_NullEntity() {
        Profile result = profileEntityMapper.toDomain(null);
        assertNull(result);
    }

    @Test
    public void testToEntity() {
        ProfileEntity result = profileEntityMapper.toEntity(profile);

        assertNotNull(result);
        assertNull(result.getId()); // ID est ignoré lors du mapping
        assertEquals("Test Profile", result.getName());
        assertEquals("Test Description", result.getDescription());
        // Note: Le mapping de product vers productId n'est pas automatique avec MapStruct
    }

    @Test
    public void testToEntity_NullDomain() {
        ProfileEntity result = profileEntityMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    public void testToDomainList() {
        ProfileEntity entity1 = new ProfileEntity();
        entity1.setId(1L);
        entity1.setName("Profile1");
        entity1.setDescription("Description1");
        entity1.setProductId(1L);

        ProfileEntity entity2 = new ProfileEntity();
        entity2.setId(2L);
        entity2.setName("Profile2");
        entity2.setDescription("Description2");
        entity2.setProductId(2L);

        List<ProfileEntity> entities = Arrays.asList(entity1, entity2);

        List<Profile> result = profileEntityMapper.toDomainList(entities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Profile1", result.get(0).getName());
        assertEquals("Description1", result.get(0).getDescription());
        assertEquals("Profile2", result.get(1).getName());
        assertEquals("Description2", result.get(1).getDescription());
    }

    @Test
    public void testToDomainList_EmptyList() {
        List<ProfileEntity> emptyList = Arrays.asList();
        List<Profile> result = profileEntityMapper.toDomainList(emptyList);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToDomainList_NullList() {
        List<Profile> result = profileEntityMapper.toDomainList(null);
        assertNull(result);
    }

    @Test
    public void testToEntityList() {
        Profile domain1 = new Profile();
        domain1.setName("Profile1");
        domain1.setDescription("Description1");
        domain1.setProduct("Product1");

        Profile domain2 = new Profile();
        domain2.setName("Profile2");
        domain2.setDescription("Description2");
        domain2.setProduct("Product2");

        List<Profile> domains = Arrays.asList(domain1, domain2);

        List<ProfileEntity> result = profileEntityMapper.toEntityList(domains);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Profile1", result.get(0).getName());
        assertEquals("Description1", result.get(0).getDescription());
        assertNull(result.get(0).getId()); // ID ignoré
        assertEquals("Profile2", result.get(1).getName());
        assertEquals("Description2", result.get(1).getDescription());
        assertNull(result.get(1).getId()); // ID ignoré
    }

    @Test
    public void testToEntityList_EmptyList() {
        List<Profile> emptyList = Arrays.asList();
        List<ProfileEntity> result = profileEntityMapper.toEntityList(emptyList);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testToEntityList_NullList() {
        List<ProfileEntity> result = profileEntityMapper.toEntityList(null);
        assertNull(result);
    }

    @Test
    public void testProfileEntityAddProductId() {
        ProfileEntity entity = new ProfileEntity();
        ProfileEntity result = entity.addProductId(5L);

        assertEquals(5L, result.getProductId());
        assertEquals(entity, result); // Vérifie que c'est la même instance qui est retournée
    }
}
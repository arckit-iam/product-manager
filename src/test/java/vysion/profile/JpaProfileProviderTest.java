package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import secop.ppm1.adapters.in.JpaProfileProvider;
import secop.ppm1.adapters.in.JpaProfileRepository;
import secop.ppm1.adapters.in.JpaProductRepository;
import secop.ppm1.adapters.in.ProductEntity;
import secop.ppm1.adapters.in.ProfileEntity;
import secop.ppm1.adapters.in.ProfileEntityMapper;
import secop.ppm1.domain.model.Profile;

public class JpaProfileProviderTest {

    @Mock
    private JpaProfileRepository profileRepository;

    @Mock
    private ProfileEntityMapper profileEntityMapper;

    @Mock
    private JpaProductRepository productRepository;

    @InjectMocks
    private JpaProfileProvider jpaProfileProvider;

    private Profile testProfile;
    private ProfileEntity testProfileEntity;
    private ProductEntity testProductEntity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testProfile = new Profile();
        testProfile.setName("Test Profile");
        testProfile.setProduct("Test Product");
        testProfile.setDescription("Test Description");

        testProfileEntity = new ProfileEntity();
        testProfileEntity.setId(1L);
        testProfileEntity.setName("Test Profile");
        testProfileEntity.setProductId(1L);
        testProfileEntity.setDescription("Test Description");

        testProductEntity = new ProductEntity();
        testProductEntity.setId(1L);
        testProductEntity.setName("Test Product");
    }

    @Test
    public void testSaveWithProductId() {
        ProfileEntity entityWithProductId = new ProfileEntity();
        entityWithProductId.setProductId(1L);
        
        when(profileEntityMapper.toEntity(testProfile)).thenReturn(testProfileEntity);
        when(profileRepository.saveAndFlush(any(ProfileEntity.class))).thenReturn(testProfileEntity);
        when(profileEntityMapper.toDomain(testProfileEntity)).thenReturn(testProfile);

        Profile result = jpaProfileProvider.save(testProfile, 1L);

        assertNotNull(result);
        assertEquals("Test Profile", result.getName());
        verify(profileEntityMapper, times(1)).toEntity(testProfile);
        verify(profileRepository, times(1)).saveAndFlush(any(ProfileEntity.class));
        verify(profileEntityMapper, times(1)).toDomain(testProfileEntity);
    }

    @Test
    public void testSaveWithProductName() {
        when(profileEntityMapper.toEntity(testProfile)).thenReturn(testProfileEntity);
        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(profileRepository.saveAndFlush(testProfileEntity)).thenReturn(testProfileEntity);
        when(profileEntityMapper.toDomain(testProfileEntity)).thenReturn(testProfile);

        Profile result = jpaProfileProvider.save(testProfile, "Test Product");

        assertNotNull(result);
        assertEquals("Test Profile", result.getName());
        verify(profileEntityMapper, times(1)).toEntity(testProfile);
        verify(productRepository, times(1)).findByName("Test Product");
        verify(profileRepository, times(1)).saveAndFlush(testProfileEntity);
        verify(profileEntityMapper, times(1)).toDomain(testProfileEntity);
    }

    @Test
    public void testDeleteProfile() {
        when(profileEntityMapper.toEntity(testProfile)).thenReturn(testProfileEntity);
        doNothing().when(profileRepository).delete(testProfileEntity);

        jpaProfileProvider.deleteProfile(testProfile);

        verify(profileEntityMapper, times(1)).toEntity(testProfile);
        verify(profileRepository, times(1)).delete(testProfileEntity);
    }

    @Test
    public void testGetProfileByNameAndProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(profileRepository.findOneByNameAndProductId("Test Profile", 1L)).thenReturn(testProfileEntity);
        when(profileEntityMapper.toDomain(testProfileEntity)).thenReturn(testProfile);

        Profile result = jpaProfileProvider.getProfileByNameAndProduct("Test Profile", "Test Product");

        assertNotNull(result);
        assertEquals("Test Profile", result.getName());
        verify(productRepository, times(1)).findByName("Test Product");
        verify(profileRepository, times(1)).findOneByNameAndProductId("Test Profile", 1L);
        verify(profileEntityMapper, times(1)).toDomain(testProfileEntity);
    }

    @Test
    public void testGetProfilesByProduct() {
        ProfileEntity profile1 = new ProfileEntity();
        profile1.setName("Profile1");
        ProfileEntity profile2 = new ProfileEntity();
        profile2.setName("Profile2");
        
        List<ProfileEntity> profileEntities = Arrays.asList(profile1, profile2);

        Profile domainProfile1 = new Profile();
        domainProfile1.setName("Profile1");
        Profile domainProfile2 = new Profile();
        domainProfile2.setName("Profile2");
        
        List<Profile> domainProfiles = Arrays.asList(domainProfile1, domainProfile2);

        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(profileRepository.findAllByProductId(1L)).thenReturn(profileEntities);
        when(profileEntityMapper.toDomainList(profileEntities)).thenReturn(domainProfiles);

        List<Profile> result = jpaProfileProvider.getProfilesByProduct("Test Product");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Profile1", result.get(0).getName());
        assertEquals("Profile2", result.get(1).getName());
        verify(productRepository, times(1)).findByName("Test Product");
        verify(profileRepository, times(1)).findAllByProductId(1L);
        verify(profileEntityMapper, times(1)).toDomainList(profileEntities);
    }

    @Test
    public void testGetProfilesByProduct_EmptyList() {
        List<ProfileEntity> emptyProfileEntities = Arrays.asList();
        List<Profile> emptyDomainProfiles = Arrays.asList();

        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(profileRepository.findAllByProductId(1L)).thenReturn(emptyProfileEntities);
        when(profileEntityMapper.toDomainList(emptyProfileEntities)).thenReturn(emptyDomainProfiles);

        List<Profile> result = jpaProfileProvider.getProfilesByProduct("Test Product");

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(productRepository, times(1)).findByName("Test Product");
        verify(profileRepository, times(1)).findAllByProductId(1L);
        verify(profileEntityMapper, times(1)).toDomainList(emptyProfileEntities);
    }

    @Test
    public void testGetProfileByNameAndProduct_NotFound() {
        when(productRepository.findByName("Test Product")).thenReturn(testProductEntity);
        when(profileRepository.findOneByNameAndProductId("Non Existing Profile", 1L)).thenReturn(null);
        when(profileEntityMapper.toDomain(null)).thenReturn(null);

        Profile result = jpaProfileProvider.getProfileByNameAndProduct("Non Existing Profile", "Test Product");

        assertNull(result);
        verify(productRepository, times(1)).findByName("Test Product");
        verify(profileRepository, times(1)).findOneByNameAndProductId("Non Existing Profile", 1L);
        verify(profileEntityMapper, times(1)).toDomain(null);
    }
}
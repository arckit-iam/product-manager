package secop.ppm1.domain.port;

import java.util.List;

import secop.ppm1.domain.model.Profile;

public interface ProfileProvider {
    Profile save(Profile profile,Long productId);
    Profile save(Profile profile,String productName);
    Profile getProfileByNameAndProduct(String profileName, String productName);
    List<Profile> getProfilesByProduct(String productName);
    void deleteProfile(Profile profile);
}

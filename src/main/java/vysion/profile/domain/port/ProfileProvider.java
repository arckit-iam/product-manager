package vysion.profile.domain.port;

import java.util.List;

import vysion.profile.domain.model.Product;
import vysion.profile.domain.model.Profile;

public interface ProfileProvider {
    Profile save(Profile profile,Long productId);
    Profile getProfileById(Long id);
    List<Profile> getProfilesByProduct(Product product);
}

// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import secop.ppm1.domain.model.Permission;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.TypePermission;

public class PermissionTest {

    private Permission permission;
    private TypePermission typePermission;
    private Profile profile;

    @BeforeEach
    public void setUp() {
        permission = new Permission();
        typePermission = new TypePermission();
        profile = new Profile();
    }

    @Test
    public void testSetAndGetType() {
        typePermission.setName("READ");
        typePermission.setLevel(1);
        
        permission.setType(typePermission);
        
        assertEquals(typePermission, permission.getType());
        assertEquals("READ", permission.getType().getName());
        assertEquals(1, permission.getType().getLevel());
    }

    @Test
    public void testSetAndGetProfile() {
        profile.setName("Test Profile");
        profile.setProduct("Test Product");
        
        permission.setProfile(profile);
        
        assertEquals(profile, permission.getProfile());
        assertEquals("Test Profile", permission.getProfile().getName());
        assertEquals("Test Product", permission.getProfile().getProduct());
    }

    @Test
    public void testPermissionEqualsAndHashCode() {
        TypePermission type1 = new TypePermission();
        type1.setName("READ");
        type1.setLevel(1);
        
        Profile profile1 = new Profile();
        profile1.setName("Profile1");
        
        Permission permission1 = new Permission();
        permission1.setType(type1);
        permission1.setProfile(profile1);
        
        Permission permission2 = new Permission();
        permission2.setType(type1);
        permission2.setProfile(profile1);
        
        assertEquals(permission1, permission2);
        assertEquals(permission1.hashCode(), permission2.hashCode());
    }

    @Test
    public void testPermissionToString() {
        typePermission.setName("WRITE");
        typePermission.setLevel(2);
        
        profile.setName("Test Profile");
        
        permission.setType(typePermission);
        permission.setProfile(profile);
        
        String toString = permission.toString();
        assertNotNull(toString);
    }

    @Test
    public void testEmptyPermission() {
        assertNull(permission.getType());
        assertNull(permission.getProfile());
    }
}
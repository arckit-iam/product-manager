package secop.ppm1.domain.model;

import java.util.UUID;

import lombok.Data;

@Data
public class Profile {
    String product;
    String name;
    String description;
    UUID jobId;
}

package com.crms.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class LatLng {

    @Column(name = "latitude", precision = 10, scale = 7)
    private Double latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private Double longitude;

    public boolean isValid() {
        return latitude != null && longitude != null 
                && latitude >= -90 && latitude <= 90 
                && longitude >= -180 && longitude <= 180;
    }
}

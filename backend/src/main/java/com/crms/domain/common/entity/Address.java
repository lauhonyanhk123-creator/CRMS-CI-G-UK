package com.crms.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "address_line_3")
    private String addressLine3;

    @Column
    private String city;

    @Column
    private String county;

    @Column
    private String postcode;

    @Column
    private String country;

    @Column(name = "grid_reference")
    private String gridReference;

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(addressLine2);
        }
        if (addressLine3 != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(addressLine3);
        }
        if (city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (county != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(county);
        }
        if (postcode != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(postcode);
        }
        return sb.toString();
    }
}

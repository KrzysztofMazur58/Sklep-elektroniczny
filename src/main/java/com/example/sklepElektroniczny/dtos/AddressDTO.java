package com.example.sklepElektroniczny.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AddressDTO {

    private Long addressId;
    private String city;
    private String street;
    private Integer number;
    private String pincode;
}

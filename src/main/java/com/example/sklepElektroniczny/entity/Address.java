package com.example.sklepElektroniczny.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 3)
    private String city;

    @NotBlank
    @Size(min = 3)
    private String street;

    @NotBlank
    private Integer number;

    @NotBlank
    @Size(min = 6)
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users = new ArrayList<>();

    public Address(String city, String street, Integer number, String pincode) {
        this.city = city;
        this.street = street;
        this.number = number;
        this.pincode = pincode;
    }
}

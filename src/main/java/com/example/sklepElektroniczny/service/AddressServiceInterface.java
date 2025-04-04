package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.User;

import java.util.List;

public interface AddressServiceInterface {

    AddressDTO addAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAllAddresses();
}

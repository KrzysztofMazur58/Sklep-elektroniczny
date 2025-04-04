package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.Address;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.repository.AddressRepository;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService implements AddressServiceInterface{

    private final AuthUtil authUtil;
    private final AddressRepository addressRepo;
    private final ModelMapper mapper;

    public AddressService(AuthUtil authUtil, AddressRepository addressRepo, ModelMapper mapper) {
        this.authUtil = authUtil;
        this.addressRepo = addressRepo;
        this.mapper = mapper;
    }

    @Override
    public AddressDTO addAddress(AddressDTO addressDTO, User user) {
        Address address = mapper.map(addressDTO, Address.class);

        List<Address> addresses = user.getAddresses();

        addresses.add(address);
        user.setAddresses(addresses);

        address.setUser(user);
        Address savedAddress = addressRepo.save(address);

        return mapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepo.findAll();

        List<AddressDTO> addressDTOS = addresses.stream().map(address -> mapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());

        return addressDTOS;

    }
}

package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.Address;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.AddressRepository;
import com.example.sklepElektroniczny.repository.UserRepository;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService implements AddressServiceInterface{

    private final AuthUtil authUtil;
    private final AddressRepository addressRepo;
    private final ModelMapper mapper;
    private final UserRepository userRepo;

    public AddressService(AuthUtil authUtil, AddressRepository addressRepo, ModelMapper mapper, UserRepository userRepo) {
        this.authUtil = authUtil;
        this.addressRepo = addressRepo;
        this.mapper = mapper;
        this.userRepo = userRepo;
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

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Optional<Address> addressOptional = addressRepo.findById(addressId);

        if (addressOptional.isEmpty()) {
            throw new ResourceNotFoundException("Address", "addressId", addressId);
        }

        Address address = addressOptional.get();
        return mapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = Optional.ofNullable(user.getAddresses())
                .orElse(List.of());

        return addresses.stream()
                .map(address -> mapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Optional<Address> fetchedAddressOptional = addressRepo.findById(addressId);

        if (fetchedAddressOptional.isEmpty()) {
            throw new ResourceNotFoundException("Address", "addressId", addressId);
        }

        Address fetchedAddress = fetchedAddressOptional.get();

        fetchedAddress.setCity(addressDTO.getCity());
        fetchedAddress.setStreet(addressDTO.getStreet());
        fetchedAddress.setNumber(addressDTO.getNumber());
        fetchedAddress.setPincode(addressDTO.getPincode());

        Address updatedAddress = addressRepo.save(fetchedAddress);

        User user  = fetchedAddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepo.save(user);

        return mapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Optional<Address> fetchedAddressOptional = addressRepo.findById(addressId);

        if (fetchedAddressOptional.isEmpty()) {
            throw new ResourceNotFoundException("Address", "addressId", addressId);
        }

        Address fetchedAddress = fetchedAddressOptional.get();

        User user  = fetchedAddress.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepo.save(user);

        addressRepo.delete(fetchedAddress);

        return "Address with id " + addressId + " deleted";
    }
}

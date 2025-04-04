package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.service.AddressService;
import com.example.sklepElektroniczny.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    private final AddressService addressService;
    private final AuthUtil authUtil;

    @Autowired
    public AddressController(AddressService addressService, AuthUtil authUtil) {
        this.addressService = addressService;
        this.authUtil = authUtil;
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO>  addAddress(@Valid @RequestBody AddressDTO addressDTO){
        User user = authUtil.getCurrentUser();
        AddressDTO addressDTO1 = addressService.addAddress(addressDTO, user);

        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddress(){

        List<AddressDTO> addresses = addressService.getAllAddresses();

        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAllAddressById(@PathVariable Long addressId){

        AddressDTO addressDTO = addressService.getAddressById(addressId);

        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){

        User user = authUtil.getCurrentUser();
        List<AddressDTO> addresses = addressService.getUserAddresses(user);

        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){

        AddressDTO addressDTO1 = addressService.updateAddressById(addressId, addressDTO);

        return new ResponseEntity<>(addressDTO1, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){

        String response = addressService.deleteAddress(addressId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

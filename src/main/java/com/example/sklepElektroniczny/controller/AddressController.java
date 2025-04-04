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
}

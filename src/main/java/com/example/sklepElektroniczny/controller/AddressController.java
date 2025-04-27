package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.service.AddressService;
import com.example.sklepElektroniczny.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Dodaj nowy adres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Adres został dodany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych wejściowych")
    })
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO>  addAddress(@Valid @RequestBody AddressDTO addressDTO){
        User user = authUtil.getCurrentUser();
        AddressDTO addressDTO1 = addressService.addAddress(addressDTO, user);

        return new ResponseEntity<>(addressDTO1, HttpStatus.CREATED);
    }

    @Operation(summary = "Pobierz wszystkie adresy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adresy zostały pobrane pomyślnie")
    })
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddress(){

        List<AddressDTO> addresses = addressService.getAllAddresses();

        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @Operation(summary = "Pobierz adres po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres został pobrany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Adres nie znaleziony")
    })
    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAllAddressById( @Parameter(description = "ID adresu", example = "1")@PathVariable Long addressId){

        AddressDTO addressDTO = addressService.getAddressById(addressId);

        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @Operation(summary = "Pobierz wszystkie adresy aktualnie zalogowanego użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adresy użytkownika zostały pobrane pomyślnie")
    })
    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){

        User user = authUtil.getCurrentUser();
        List<AddressDTO> addresses = addressService.getUserAddresses(user);

        return new ResponseEntity<>(addresses, HttpStatus.OK);
    }

    @Operation(summary = "Aktualizuj adres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres został zaktualizowany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Adres nie znaleziony")
    })
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@Parameter(description = "ID adresu", example = "1") @PathVariable Long addressId, @RequestBody AddressDTO addressDTO){

        AddressDTO addressDTO1 = addressService.updateAddressById(addressId, addressDTO);

        return new ResponseEntity<>(addressDTO1, HttpStatus.OK);
    }

    @Operation(summary = "Usuń adres")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adres został usunięty pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Adres nie znaleziony")
    })
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(@Parameter(description = "ID adresu", example = "1") @PathVariable Long addressId){

        String response = addressService.deleteAddress(addressId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

package com.example.sklepElektroniczny.controller;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.service.AddressService;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class AddressControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private AddressController addressController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User mockUser;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(addressController).build();
        mockUser = new User();
        mockUser.setEmail("test@example.com");
    }

    @Test
    public void testAddAddress_Success() throws Exception {
        AddressDTO inputDto = new AddressDTO(null, "City", "Street", 12, "12345");
        AddressDTO returnedDto = new AddressDTO(1L, "City", "Street", 12, "12345");

        when(authUtil.getCurrentUser()).thenReturn(mockUser);
        when(addressService.addAddress(any(AddressDTO.class), eq(mockUser))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressId").value(1))
                .andExpect(jsonPath("$.city").value("City"))
                .andExpect(jsonPath("$.street").value("Street"))
                .andExpect(jsonPath("$.number").value(12))
                .andExpect(jsonPath("$.pincode").value("12345"));
    }

    @Test
    public void testGetAllAddress_Success() throws Exception {
        AddressDTO dto = new AddressDTO(1L, "City", "Street", 12, "12345");
        List<AddressDTO> list = Collections.singletonList(dto);

        when(addressService.getAllAddresses()).thenReturn(list);

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressId").value(1));
    }

    @Test
    public void testGetAllAddressById_Success() throws Exception {
        AddressDTO dto = new AddressDTO(1L, "City", "Street", 12, "12345");

        when(addressService.getAddressById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1));
    }

    @Test
    public void testGetUserAddresses_Success() throws Exception {
        AddressDTO dto = new AddressDTO(1L, "City", "Street", 12, "12345");
        List<AddressDTO> list = Collections.singletonList(dto);

        when(authUtil.getCurrentUser()).thenReturn(mockUser);
        when(addressService.getUserAddresses(mockUser)).thenReturn(list);

        mockMvc.perform(get("/api/users/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].addressId").value(1));
    }

    @Test
    public void testUpdateAddressById_Success() throws Exception {
        AddressDTO inputDto = new AddressDTO(null, "NewCity", "NewStreet", 99, "99999");
        AddressDTO returnedDto = new AddressDTO(1L, "NewCity", "NewStreet", 99, "99999");

        when(addressService.updateAddressById(eq(1L), any(AddressDTO.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(1))
                .andExpect(jsonPath("$.city").value("NewCity"));
    }

    @Test
    public void testDeleteAddress_Success() throws Exception {
        when(addressService.deleteAddress(1L)).thenReturn("Deleted");

        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }
}


package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.AddressDTO;
import com.example.sklepElektroniczny.entity.Address;
import com.example.sklepElektroniczny.entity.User;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.AddressRepository;
import com.example.sklepElektroniczny.repository.UserRepository;
import com.example.sklepElektroniczny.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    @Mock
    private AuthUtil authUtil;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addAddress_success() {
        User user = new User();
        user.setAddresses(new java.util.ArrayList<>());

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity("City");
        addressDTO.setStreet("Street");
        addressDTO.setNumber(123);
        addressDTO.setPincode("99999");

        Address mappedAddress = new Address();
        mappedAddress.setCity("City");

        when(mapper.map(addressDTO, Address.class)).thenReturn(mappedAddress);
        when(addressRepo.save(mappedAddress)).thenReturn(mappedAddress);
        when(mapper.map(mappedAddress, AddressDTO.class)).thenReturn(addressDTO);

        AddressDTO result = addressService.addAddress(addressDTO, user);

        assertEquals("City", result.getCity());
        assertTrue(user.getAddresses().contains(mappedAddress));
        verify(addressRepo).save(mappedAddress);
    }

    @Test
    void getAllAddresses_returnsMappedList() {
        Address address1 = new Address();
        Address address2 = new Address();

        List<Address> addresses = List.of(address1, address2);
        when(addressRepo.findAll()).thenReturn(addresses);

        AddressDTO dto1 = new AddressDTO();
        AddressDTO dto2 = new AddressDTO();

        when(mapper.map(address1, AddressDTO.class)).thenReturn(dto1);
        when(mapper.map(address2, AddressDTO.class)).thenReturn(dto2);

        List<AddressDTO> result = addressService.getAllAddresses();

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void getAddressById_found_returnsDTO() {
        Long addressId = 1L;
        Address address = new Address();
        when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));

        AddressDTO dto = new AddressDTO();
        when(mapper.map(address, AddressDTO.class)).thenReturn(dto);

        AddressDTO result = addressService.getAddressById(addressId);

        assertNotNull(result);
        assertEquals(dto, result);
    }

    @Test
    void getAddressById_notFound_throws() {
        Long addressId = 1L;
        when(addressRepo.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressById(addressId));
    }

    @Test
    void getUserAddresses_returnsMappedList() {
        Address addr1 = new Address();
        Address addr2 = new Address();
        User user = new User();
        user.setAddresses(List.of(addr1, addr2));

        AddressDTO dto1 = new AddressDTO();
        AddressDTO dto2 = new AddressDTO();

        when(mapper.map(addr1, AddressDTO.class)).thenReturn(dto1);
        when(mapper.map(addr2, AddressDTO.class)).thenReturn(dto2);

        List<AddressDTO> result = addressService.getUserAddresses(user);

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    @Test
    void getUserAddresses_noAddresses_returnsEmptyList() {
        User user = new User();
        user.setAddresses(null);

        List<AddressDTO> result = addressService.getUserAddresses(user);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateAddressById_success() {
        Long addressId = 1L;
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity("NewCity");
        addressDTO.setStreet("NewStreet");
        addressDTO.setNumber(456);
        addressDTO.setPincode("11111");

        Address fetchedAddress = new Address();
        fetchedAddress.setAddressId(addressId);
        fetchedAddress.setCity("OldCity");
        User user = new User();
        user.setAddresses(new java.util.ArrayList<>());
        user.getAddresses().add(fetchedAddress);
        fetchedAddress.setUser(user);

        when(addressRepo.findById(addressId)).thenReturn(Optional.of(fetchedAddress));
        when(addressRepo.save(fetchedAddress)).thenReturn(fetchedAddress);
        when(mapper.map(fetchedAddress, AddressDTO.class)).thenReturn(addressDTO);
        when(userRepo.save(user)).thenReturn(user);

        AddressDTO updated = addressService.updateAddressById(addressId, addressDTO);

        assertEquals("NewCity", fetchedAddress.getCity());
        assertTrue(user.getAddresses().stream().anyMatch(a -> a.getAddressId().equals(addressId)));
        assertEquals(addressDTO, updated);
        verify(userRepo).save(user);
    }

    @Test
    void updateAddressById_notFound_throws() {
        Long addressId = 1L;
        AddressDTO addressDTO = new AddressDTO();

        when(addressRepo.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddressById(addressId, addressDTO));
    }

    @Test
    void deleteAddress_success() {
        Long addressId = 1L;
        Address fetchedAddress = new Address();
        fetchedAddress.setAddressId(addressId);
        User user = new User();
        user.setAddresses(new java.util.ArrayList<>());
        user.getAddresses().add(fetchedAddress);
        fetchedAddress.setUser(user);

        when(addressRepo.findById(addressId)).thenReturn(Optional.of(fetchedAddress));
        doNothing().when(addressRepo).delete(fetchedAddress);
        when(userRepo.save(user)).thenReturn(user);

        String result = addressService.deleteAddress(addressId);

        assertTrue(result.contains(addressId.toString()));
        assertFalse(user.getAddresses().stream().anyMatch(a -> a.getAddressId().equals(addressId)));
        verify(addressRepo).delete(fetchedAddress);
        verify(userRepo).save(user);
    }

    @Test
    void deleteAddress_notFound_throws() {
        Long addressId = 1L;
        when(addressRepo.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(addressId));
    }
}


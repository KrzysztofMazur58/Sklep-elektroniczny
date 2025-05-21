package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderElementDTO;
import com.example.sklepElektroniczny.entity.*;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private CartRepository cartRepo;

    @Mock
    private AddressRepository addressRepo;

    @Mock
    private OrderElementRepository elementRepo;

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private PaymentRepository paymentRepo;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper mapper;

    @Mock
    private ProductRepository productRepo;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_success() {
        String email = "test@example.com";
        Long addressId = 1L;

        User user = new User();
        user.setEmail(email);

        Cart cart = new Cart();
        cart.setCartId(10L);
        cart.setUser(user);
        cart.setTotalPrice(100.0);

        Product product = new Product();
        product.setProductId(5L);
        product.setQuantity(20);

        CartElement cartElement = new CartElement();
        cartElement.setProduct(product);
        cartElement.setQuantity(2);
        cartElement.setDiscount(0.0);
        cartElement.setPrice(50.0);
        cart.setCartElements(List.of(cartElement));

        Address address = new Address();
        address.setAddressId(addressId);

        Order newOrder = new Order();
        newOrder.setEmail(email);
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setTotalPrice(BigDecimal.valueOf(100).setScale(2));
        newOrder.setStatus("Order accepted");
        newOrder.setAddress(address);

        Payment payment = new Payment("card", "pg123", "success", "ok", "Stripe");
        payment.setOrder(newOrder);

        Order storedOrder = new Order();
        storedOrder.setOrderId(100L);
        storedOrder.setEmail(email);
        storedOrder.setOrderDate(LocalDate.now());
        storedOrder.setTotalPrice(BigDecimal.valueOf(100).setScale(2));
        storedOrder.setStatus("Order accepted");
        storedOrder.setAddress(address);
        storedOrder.setPayment(payment);

        OrderElement orderElement = new OrderElement();
        orderElement.setProduct(product);
        orderElement.setQuantity(2);
        orderElement.setDiscount(0.0);
        orderElement.setOrderedProductPrice(50.0); // POPRAWKA
        orderElement.setOrder(storedOrder);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setEmail(email);
        orderDTO.setOrderDate(LocalDate.now());
        orderDTO.setTotalPrice(BigDecimal.valueOf(100).setScale(2));
        orderDTO.setStatus("Order accepted");
        orderDTO.setAddressId(addressId);
        orderDTO.setOrderElements(new ArrayList<>());

        OrderElementDTO orderElementDTO = new OrderElementDTO();

        when(cartRepo.findCartByEmail(email)).thenReturn(cart);
        when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));
        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
        when(orderRepo.save(any(Order.class))).thenReturn(storedOrder);
        when(elementRepo.saveAll(anyList())).thenReturn(List.of(orderElement));
        when(cartService.deleteProductFromCart(anyLong(), anyLong())).thenReturn(null); // POPRAWKA
        when(productRepo.save(any(Product.class))).thenReturn(product);
        when(mapper.map(storedOrder, OrderDTO.class)).thenReturn(orderDTO);
        when(mapper.map(orderElement, OrderElementDTO.class)).thenReturn(orderElementDTO);

        OrderDTO result = orderService.createOrder(
                email,
                addressId,
                "card",
                "Stripe",
                "pg123",
                "success",
                "ok"
        );

        assertNotNull(result);
        assertEquals(addressId, result.getAddressId());
        assertEquals(email, result.getEmail());
        assertEquals(1, result.getOrderElements().size());
        verify(cartRepo).findCartByEmail(email);
        verify(addressRepo).findById(addressId);
        verify(paymentRepo).save(any(Payment.class));
        verify(orderRepo).save(any(Order.class));
        verify(elementRepo).saveAll(anyList());
        verify(cartService).deleteProductFromCart(cart.getCartId(), product.getProductId());
        verify(productRepo).save(product);
    }
    
    @Test
    void createOrder_cartNotFound_throwsResourceNotFound() {
        String email = "notfound@example.com";
        when(cartRepo.findCartByEmail(email)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(
                email,
                1L,
                "card",
                "Stripe",
                "pg123",
                "success",
                "ok"
        ));
    }

    @Test
    void createOrder_addressNotFound_throwsResourceNotFound() {
        String email = "test@example.com";
        Long addressId = 1L;

        User user = new User();
        user.setEmail(email);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartElements(List.of(new CartElement()));

        when(cartRepo.findCartByEmail(email)).thenReturn(cart);
        when(addressRepo.findById(addressId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(
                email,
                addressId,
                "card",
                "Stripe",
                "pg123",
                "success",
                "ok"
        ));
    }

    @Test
    void getOrdersByEmail_returnsOrderDTOList() {
        String email = "test@example.com";
        Order order1 = new Order();
        Order order2 = new Order();

        when(orderRepo.findByEmail(email)).thenReturn(List.of(order1, order2));

        OrderDTO dto1 = new OrderDTO();
        OrderDTO dto2 = new OrderDTO();

        when(mapper.map(order1, OrderDTO.class)).thenReturn(dto1);
        when(mapper.map(order2, OrderDTO.class)).thenReturn(dto2);

        List<OrderDTO> results = orderService.getOrdersByEmail(email);

        assertEquals(2, results.size());
        assertTrue(results.contains(dto1));
        assertTrue(results.contains(dto2));
        verify(orderRepo).findByEmail(email);
    }

    @Test
    void getAllOrders_returnsOrderDTOList() {
        Order order1 = new Order();
        Order order2 = new Order();

        when(orderRepo.findAll()).thenReturn(List.of(order1, order2));

        OrderDTO dto1 = new OrderDTO();
        OrderDTO dto2 = new OrderDTO();

        when(mapper.map(order1, OrderDTO.class)).thenReturn(dto1);
        when(mapper.map(order2, OrderDTO.class)).thenReturn(dto2);

        List<OrderDTO> results = orderService.getAllOrders();

        assertEquals(2, results.size());
        assertTrue(results.contains(dto1));
        assertTrue(results.contains(dto2));
        verify(orderRepo).findAll();
    }

    @Test
    void updateOrderStatus_success() {
        Long orderId = 1L;
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("Pending");

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepo.save(order)).thenReturn(order);

        orderService.updateOrderStatus(orderId, "Shipped");

        assertEquals("Shipped", order.getStatus());
        verify(orderRepo).findById(orderId);
        verify(orderRepo).save(order);
    }

    @Test
    void updateOrderStatus_notFound_throws() {
        Long orderId = 1L;

        when(orderRepo.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrderStatus(orderId, "Shipped"));
    }
}





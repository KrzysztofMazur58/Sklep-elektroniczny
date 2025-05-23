package com.example.sklepElektroniczny.service;

import com.example.sklepElektroniczny.dtos.OrderDTO;
import com.example.sklepElektroniczny.dtos.OrderElementDTO;
import com.example.sklepElektroniczny.entity.*;
import com.example.sklepElektroniczny.exceptions.APIException;
import com.example.sklepElektroniczny.exceptions.ResourceNotFoundException;
import com.example.sklepElektroniczny.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService implements OrderServiceInterface {

    private final CartRepository cartRepo;
    private final AddressRepository addressRepo;
    private final OrderElementRepository elementRepo;
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final CartService cartService;
    private final ModelMapper mapper;
    private final ProductRepository productRepo;

    public OrderService(
            CartRepository cartStorage,
            AddressRepository locationRepo,
            OrderElementRepository elementRepo,
            OrderRepository orderData,
            PaymentRepository paymentData,
            CartService cartOps,
            ModelMapper mapper,
            ProductRepository inventoryRepo
    ) {
        this.cartRepo = cartStorage;
        this.addressRepo = locationRepo;
        this.elementRepo = elementRepo;
        this.orderRepo = orderData;
        this.paymentRepo = paymentData;
        this.cartService = cartOps;
        this.mapper = mapper;
        this.productRepo = inventoryRepo;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(
            String emailId,
            Long addressId,
            String paymentMethod,
            String paymentGatewayName,
            String gatewayPaymentId,
            String gatewayStatus,
            String gatewayResponseMessage
    ) {
        Cart userCart = cartRepo.findCartByEmail(emailId);
        if (userCart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Order newOrder = new Order();
        newOrder.setEmail(emailId);
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setTotalPrice(BigDecimal.valueOf(userCart.getTotalPrice()).setScale(2, RoundingMode.HALF_UP));
        newOrder.setStatus("Order accepted");
        newOrder.setAddress(address);

        Payment payment = new Payment(paymentMethod, gatewayPaymentId, gatewayStatus, gatewayResponseMessage, paymentGatewayName);
        payment.setOrder(newOrder);
        payment = paymentRepo.save(payment);
        newOrder.setPayment(payment);

        Order storedOrder = orderRepo.save(newOrder);

        List<CartElement> elementsInCart = userCart.getCartElements();
        if (elementsInCart.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderElement> finalOrderElements = new ArrayList<>();
        for (CartElement entry : elementsInCart) {
            OrderElement element = new OrderElement();
            element.setProduct(entry.getProduct());
            element.setQuantity(entry.getQuantity());
            element.setDiscount(entry.getDiscount());
            element.setOrderedProductPrice(entry.getPrice());
            element.setOrder(storedOrder);
            finalOrderElements.add(element);
        }

        finalOrderElements = elementRepo.saveAll(finalOrderElements);

        elementsInCart.forEach(entry -> {
            int qty = entry.getQuantity();
            Product itemProduct = entry.getProduct();

            itemProduct.setQuantity(itemProduct.getQuantity() - qty);
            productRepo.save(itemProduct);

            cartService.deleteProductFromCart(userCart.getCartId(), itemProduct.getProductId());
        });

        OrderDTO result = mapper.map(storedOrder, OrderDTO.class);
        finalOrderElements.forEach(oi -> result.getOrderElements().add(mapper.map(oi, OrderElementDTO.class)));
        result.setAddressId(addressId);

        return result;
    }

    @Override
    public List<OrderDTO> getOrdersByEmail(String email) {
        List<Order> orders = orderRepo.findByEmail(email);
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = mapper.map(order, OrderDTO.class);

        if (order.getOrderElements() != null) {
            dto.setOrderElements(order.getOrderElements().stream()
                    .map(oe -> mapper.map(oe, OrderElementDTO.class))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        order.setStatus(newStatus);
        orderRepo.save(order);
    }

}



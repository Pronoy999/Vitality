package com.vitality.api.service;

import com.vitality.api.entities.*;
import com.vitality.api.repositories.OrderRepository;
import com.vitality.common.dtos.*;
import com.vitality.common.exceptions.InvalidRequestException;
import com.vitality.common.utils.FinanceUtils;
import com.vitality.common.utils.ResponseGenerator;
import com.vitality.common.utils.Validators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PatientService patientService;
    private final InventoryService inventoryService;

    /**
     * Method to create the Order.
     *
     * @param request: the order creation request.
     * @return
     */
    public ResponseEntity<?> createOrder(CreateOrderRequest request) {
        try {
            Validators.validateOrderRequest(request);
            Patient patient;
            if (Objects.nonNull(request.getPatientId())) {
                patient = patientService.searchPatient(null, null, null, null, request.getPatientId());
            } else if (StringUtils.hasLength(request.getPatientFirstName()) || StringUtils.hasLength(request.getPatientLastName())) {
                patient = patientService.searchPatient(request.getPatientFirstName(), request.getPatientLastName(), null, null, null);
            } else {
                throw new InvalidRequestException("Patient Name can't be empty.");
            }
            List<OrderRequestItems> orderRequestItems = request.getOrderRequestItems();
            List<OrderItems> orderItems = getAvailableOrderItems(orderRequestItems);
            Order order = getOrderDetails(request, patient, orderItems);
            order = orderRepository.save(order);
            log.info("Order Created with ID: {} and items: {}", order.getId(), orderItems.size());
            CreateOrderResponse response = new CreateOrderResponse();
            response.setOrderId(order.getId());
            return ResponseGenerator.generateSuccessResponse(response, HttpStatus.CREATED);
        } catch (InvalidRequestException e) {
            log.error(e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseGenerator.generateFailureResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }
    }

    private Order getOrderDetails(CreateOrderRequest request, Patient patient, List<OrderItems> orderItems) {
        Order order = new Order();
        order.setPatient(patient);
        order.setOrderItems(orderItems);
        OrderTotalPrice orderTotalPrice = getOrderTotalPrice(orderItems, request.getPlatformFee(), request.getDeliveryFee());
        order.setTotalItemPrice(orderTotalPrice.getItemTotalPrice());
        order.setTotalDiscount(orderTotalPrice.getTotalDiscount());
        order.setTotalTaxAmount(orderTotalPrice.getTotalTaxAmount());
        order.setPlatformFee(request.getPlatformFee());
        order.setDeliveryFee(request.getDeliveryFee());
        order.setRoundOffAmount(orderTotalPrice.getRoundOffAmount());
        order.setTotalPrice(orderTotalPrice.getTotalPrice());
        order.setOrderStatus(OrderStatus.PROCESSING);
        return order;
    }

    /**
     * Method to fetch and filter Items from Inventory which are available.
     *
     * @param orderRequestItems: the Order items.
     * @return the Available {@link Inventory} items.
     */
    private List<OrderItems> getAvailableOrderItems(List<OrderRequestItems> orderRequestItems) {
        Set<Long> ids = orderRequestItems.stream()
                .map(OrderRequestItems::getInventoryId)
                .collect(Collectors.toSet());

        Map<Long, Inventory> inventoryMap = inventoryService.getItemsById(ids.stream().toList())
                .stream()
                .collect(Collectors.toMap(Inventory::getId, Function.identity()));

        if (inventoryMap.size() != ids.size()) {
            throw new InvalidRequestException("Invalid Inventory Item");
        }

        return orderRequestItems.stream()
                .map(item -> {
                    Inventory inventory = inventoryMap.get(item.getInventoryId());
                    if (inventory.getQuantityAvailable().compareTo(item.getQuantity()) < 0) {
                        log.error("Order for Item {} can't be full filled", inventory.getItemDescription());
                        throw new InvalidRequestException("Insufficient quantity for Item: " + inventory.getItemDescription());
                    }
                    OrderItems orderItem = new OrderItems();
                    orderItem.setItemId(inventory.getId());
                    orderItem.setQuantity(item.getQuantity());
                    BigDecimal purchasePrice = inventory.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity().longValue()));
                    OrderItemPrice orderItemPrice = FinanceUtils.getOrderItemPrice(purchasePrice, inventory.getTaxPercentage(), inventory.getMrp(), item.getMarkupPercentage());
                    orderItem.setSgstAmount(orderItemPrice.getSgstAmount());
                    orderItem.setSgstPercentage(orderItemPrice.getSgstPercentage());
                    orderItem.setCgstAmount(orderItemPrice.getCgstAmount());
                    orderItem.setCgstPercentage(orderItemPrice.getCgstPercentage());
                    orderItem.setTotalTaxAmount(orderItemPrice.getTotalTaxAmount());
                    orderItem.setItemTotalPrice(orderItemPrice.getTotalPrice());
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    private OrderTotalPrice getOrderTotalPrice(List<OrderItems> orderItems, BigDecimal platformFee, BigDecimal deliveryFee) {
        OrderTotalPrice orderTotalPrice = new OrderTotalPrice();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal itemTotalPrice = BigDecimal.ZERO;
        for (OrderItems item : orderItems) {
            itemTotalPrice = itemTotalPrice.add(item.getItemPrice());
            totalTax = totalTax.add(item.getTotalTaxAmount());
            totalDiscount = totalDiscount.add(item.getItemDiscount());
            totalPrice = totalPrice.add(item.getItemTotalPrice());
        }
        totalPrice = totalPrice.add(platformFee).add(deliveryFee);
        orderTotalPrice.setTotalTaxAmount(totalTax);
        orderTotalPrice.setTotalDiscount(totalDiscount);
        orderTotalPrice.setItemTotalPrice(itemTotalPrice);
        orderTotalPrice.setRoundOffAmount(FinanceUtils.calculateRoundOffAmount(totalPrice));
        totalPrice = totalPrice.add(orderTotalPrice.getRoundOffAmount());
        orderTotalPrice.setTotalPrice(totalPrice);
        return orderTotalPrice;
    }
}

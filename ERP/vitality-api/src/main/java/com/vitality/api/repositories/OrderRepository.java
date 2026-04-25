package com.vitality.api.repositories;

import com.vitality.api.entities.Order;
import com.vitality.api.entities.OrderInvoiceView;
import com.vitality.api.entities.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
                select new com.vitality.api.entities.OrderInvoiceView(
                    o.id,
                    p.firstName,
                    p.lastName,
                    o.orderDate,
            
                    o.totalItems,
                    o.totalItemPrice,
                    o.totalDiscount,
                    o.totalTaxAmount,
                    o.platformFee,
                    o.deliveryFee,
                    o.roundOffAmount,
                    o.totalPrice,
            
                    oi.quantity,
                    i.itemDescription,
                    oi.itemPrice,
                    oi.itemDiscount,
                    oi.cgstAmount,
                    oi.sgstAmount,
                    oi.itemTotalPrice
                )
                from Order o
                join o.patient p
                join o.orderItems oi
                left join Inventory i on i.id = oi.itemId
                where o.id = :id
                  and o.isActive = true
                  and o.orderStatus = com.vitality.api.entities.OrderStatus.PROCESSING
            """)
    List<OrderInvoiceView> fetchOrderForInvoice(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("""
                update Order o
                set o.orderStatus = :status,
                o.updatedTimestamp=current_timestamp
                where o.id = :id
            """)
    int updateOrderStatusById(Long id, OrderStatus status);
}

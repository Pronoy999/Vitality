package com.vitality.common.utils;

import com.vitality.common.dtos.OrderItemPrice;
import com.vitality.common.dtos.OrderTotalPrice;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FinanceUtils {
    /**
     * Method to get the item Price with tax applied and discount subtracted. If discount is null, it will be treated as zero.
     *
     * @param itemPrice: The base price of the item before tax and discount.
     * @param taxRate    The tax rate to be applied on the item price. It should be in decimal form (e.g., 0.18 for 18% tax).
     * @param discount   The discount amount to be subtracted from the item price after tax is applied. It should be in the same currency as the item price.
     * @return the final price of the item after applying tax and subtracting discount.
     */
    public static BigDecimal getItemPriceWithTax(BigDecimal itemPrice, BigDecimal taxRate, BigDecimal discount) {
        if (itemPrice == null || taxRate == null) {
            throw new IllegalArgumentException("Item price and tax rate cannot be null");
        }
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        return itemPrice.add(itemPrice.multiply(taxRate)).subtract(discount);
    }

    /**
     * Method to calculate the Individual Items Price.
     * It will add the tax percentage for SGST and CGST based on the tax percentage.
     * Then hike the price by markup percentage.
     *
     * @param purchasePrice:    The Purchase price of that item.
     * @param taxPercentage:    the tax percentage applicable for that item.
     * @param mrp:              the mrp of that item.
     * @param markupPercentage: The Price Hike percentage.
     * @return the {@link OrderItemPrice} for that Item.
     */
    public static OrderItemPrice getOrderItemPrice(BigDecimal purchasePrice, BigDecimal taxPercentage, BigDecimal mrp, BigDecimal markupPercentage) {
        OrderItemPrice orderItemPrice = new OrderItemPrice();
        markupPercentage = normalizePercentage(markupPercentage);
        BigDecimal sellingPrice = purchasePrice.add(purchasePrice.multiply(markupPercentage).divide(BigDecimal.valueOf(100),
                2, RoundingMode.HALF_UP));
        orderItemPrice.setTotalItemPrice(sellingPrice);
        if (sellingPrice.longValue() > mrp.longValue()) {
            sellingPrice = mrp;
        } else {
            orderItemPrice.setTotalDiscount(mrp.subtract(sellingPrice));
        }
        if (taxPercentage.compareTo(BigDecimal.valueOf(5)) == 0) {
            orderItemPrice.setSgstPercentage(BigDecimal.valueOf(2.5));
            orderItemPrice.setSgstAmount(getTaxAmount(orderItemPrice.getSgstPercentage(), sellingPrice));
            orderItemPrice.setCgstPercentage(BigDecimal.valueOf(2.5));
            orderItemPrice.setCgstAmount(getTaxAmount(orderItemPrice.getCgstPercentage(), sellingPrice));
        } else if (taxPercentage.compareTo(BigDecimal.valueOf(12)) == 0) {
            orderItemPrice.setSgstPercentage(BigDecimal.valueOf(6));
            orderItemPrice.setSgstAmount(getTaxAmount(orderItemPrice.getSgstPercentage(), sellingPrice));
            orderItemPrice.setCgstPercentage(BigDecimal.valueOf(6));
            orderItemPrice.setCgstAmount(getTaxAmount(orderItemPrice.getCgstPercentage(), sellingPrice));
        } else if (taxPercentage.compareTo(BigDecimal.valueOf(18)) == 0) {
            orderItemPrice.setSgstPercentage(BigDecimal.valueOf(9));
            orderItemPrice.setSgstAmount(getTaxAmount(orderItemPrice.getSgstPercentage(), sellingPrice));
            orderItemPrice.setCgstPercentage(BigDecimal.valueOf(9));
            orderItemPrice.setCgstAmount(getTaxAmount(orderItemPrice.getCgstPercentage(), sellingPrice));
        } else if (taxPercentage.compareTo(BigDecimal.valueOf(28)) == 0) {
            orderItemPrice.setSgstPercentage(BigDecimal.valueOf(14));
            orderItemPrice.setSgstAmount(getTaxAmount(orderItemPrice.getCgstPercentage(), sellingPrice));
            orderItemPrice.setCgstPercentage(BigDecimal.valueOf(14));
            orderItemPrice.setCgstAmount(getTaxAmount(orderItemPrice.getCgstPercentage(), sellingPrice));
        }
        BigDecimal totalTax = orderItemPrice.getSgstAmount().add(orderItemPrice.getCgstAmount());
        orderItemPrice.setTotalTaxAmount(totalTax);
        BigDecimal totalPrice = sellingPrice.add(totalTax);
        orderItemPrice.setTotalPrice(totalPrice);
        return orderItemPrice;
    }

    private static BigDecimal getTaxAmount(BigDecimal taxPercentage, BigDecimal price) {
        return price.multiply(taxPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateRoundOffAmount(BigDecimal totalPrice) {
        BigDecimal rounded = totalPrice.setScale(0, RoundingMode.HALF_UP);
        return rounded.subtract(totalPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal normalizePercentage(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ONE) <= 0) {
            return value.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

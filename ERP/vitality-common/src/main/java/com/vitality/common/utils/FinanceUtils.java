package com.vitality.common.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FinanceUtils {
    /**
     * Method to get the item Price with tax applied and discount subtracted. If discount is null, it will be treated as zero.
     *
     * @param itemPrice: The base price of the item before tax and discount.
     * @param taxRate   The tax rate to be applied on the item price. It should be in decimal form (e.g., 0.18 for 18% tax).
     * @param discount  The discount amount to be subtracted from the item price after tax is applied. It should be in the same currency as the item price.
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
}

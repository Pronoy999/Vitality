package com.vitality.common.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {
    /**
     * Method to get the unique key for an item based on item description, batch number and expiry date.
     *
     * @param itemDesc:    the item description.
     * @param batchNumber: the batch number.
     * @param expiryDate:  the expiry date.
     * @return the unique key for the item.
     */
    public static String getUniqueItemKey(String itemDesc, String batchNumber, LocalDate expiryDate) {
        if (StringUtils.hasLength(itemDesc) && StringUtils.hasLength(batchNumber) && expiryDate != null) {
            return itemDesc.trim().toLowerCase() + "_" + batchNumber.trim().toLowerCase() + "_" + expiryDate;
        }
        return null;
    }
}

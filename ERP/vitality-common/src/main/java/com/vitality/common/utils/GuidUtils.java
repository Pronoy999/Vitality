package com.vitality.common.utils;

import java.util.UUID;

public class GuidUtils {
    /**
     * Method to generate a GUID.
     *
     * @return the generated Guid in {@link String} format.
     */
    public static String generateGuid() {
        return UUID.randomUUID().toString();
    }
}

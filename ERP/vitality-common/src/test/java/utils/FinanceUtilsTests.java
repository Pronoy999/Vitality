package utils;

import com.vitality.common.dtos.OrderItemPrice;
import com.vitality.common.utils.FinanceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class FinanceUtilsTests {
    @Test
    public void testFinanceUtils() {
        OrderItemPrice orderItemPrice = FinanceUtils.getOrderItemPrice(BigDecimal.valueOf(100), BigDecimal.valueOf(12), BigDecimal.valueOf(150), BigDecimal.valueOf(2));
        Assertions.assertNotNull(orderItemPrice);
        Assertions.assertEquals(BigDecimal.valueOf(6.12), orderItemPrice.getCgstAmount());
        Assertions.assertEquals(BigDecimal.valueOf(6.12), orderItemPrice.getSgstAmount());
        Assertions.assertEquals(BigDecimal.valueOf(6), orderItemPrice.getCgstPercentage());
        Assertions.assertEquals(BigDecimal.valueOf(6), orderItemPrice.getSgstPercentage());
        Assertions.assertEquals(48.00, orderItemPrice.getTotalDiscount().doubleValue());
        Assertions.assertEquals(114.24, orderItemPrice.getTotalPrice().doubleValue());
    }
}

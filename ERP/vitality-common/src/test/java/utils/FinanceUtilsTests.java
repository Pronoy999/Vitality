package utils;

import com.vitality.common.dtos.OrderItemPrice;
import com.vitality.common.utils.FinanceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class FinanceUtilsTests {
    @Test
    public void testFinanceUtils() {
        OrderItemPrice orderItemPrice = FinanceUtils.getOrderItemPrice(BigDecimal.valueOf(100), BigDecimal.valueOf(5), BigDecimal.valueOf(150), BigDecimal.valueOf(2));
        Assertions.assertNotNull(orderItemPrice);
        Assertions.assertEquals(orderItemPrice.getCgstAmount(), BigDecimal.valueOf(2.55));
        Assertions.assertEquals(orderItemPrice.getSgstAmount(), BigDecimal.valueOf(2.55));
        Assertions.assertEquals(BigDecimal.valueOf(2.5), orderItemPrice.getCgstPercentage());
        Assertions.assertEquals(BigDecimal.valueOf(2.5), orderItemPrice.getSgstPercentage());
        Assertions.assertEquals(48.00, orderItemPrice.getTotalDiscount().doubleValue());
        Assertions.assertEquals(107.10, orderItemPrice.getTotalPrice().doubleValue());
    }
}

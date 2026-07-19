package vn.edu.fpt.bookstore.successfullyDat;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal nonNegative(BigDecimal value) {
        return normalize(value).max(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
}

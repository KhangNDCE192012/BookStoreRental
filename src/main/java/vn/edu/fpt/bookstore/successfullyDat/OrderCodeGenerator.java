package vn.edu.fpt.bookstore.successfullyDat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderCodeGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderCodeGenerator() {
    }

    public static String purchaseCode() {
        return "PO" + LocalDateTime.now().format(FORMATTER) + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    public static String rentalCode() {
        return "RO" + LocalDateTime.now().format(FORMATTER) + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}

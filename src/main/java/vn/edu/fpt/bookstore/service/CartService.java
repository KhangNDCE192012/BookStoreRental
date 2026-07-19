package vn.edu.fpt.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.bookstore.entity.Book;
import vn.edu.fpt.bookstore.entity.Cart;
import vn.edu.fpt.bookstore.entity.CartItem;
import vn.edu.fpt.bookstore.entity.User;
import vn.edu.fpt.bookstore.entity.enums.BookCopyStatus;
import vn.edu.fpt.bookstore.entity.enums.CartItemType;
import vn.edu.fpt.bookstore.repository.BookCopyRepository;
import vn.edu.fpt.bookstore.repository.CartItemRepository;
import vn.edu.fpt.bookstore.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {
    public static final BigDecimal PURCHASE_SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookService bookService;
    private final BookCopyRepository bookCopyRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       BookService bookService,
                       BookCopyRepository bookCopyRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookService = bookService;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional
    public Cart getOrCreate(User user) {
        return cartRepository.findByUserIdWithItems(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public void add(User user, UUID bookId, CartItemType type, int quantity, Integer rentalDays) {
        if (quantity < 1 || quantity > 20) throw new IllegalArgumentException("Số lượng phải từ 1 đến 20");
        Book book = bookService.requireActive(bookId);
        long available = bookCopyRepository.countByBook_IdAndStatus(bookId, BookCopyStatus.AVAILABLE);
        if (available < quantity) throw new IllegalArgumentException("Kho không đủ số lượng sách đang có sẵn");
        if (type == CartItemType.RENTAL) {
            if (rentalDays == null || rentalDays < 1 || rentalDays > 30) {
                throw new IllegalArgumentException("Số ngày thuê phải từ 1 đến 30");
            }
        }
        Cart cart = getOrCreate(user);
        CartItem item = cartItemRepository.findByCart_IdAndBook_IdAndItemType(cart.getId(), bookId, type)
                .orElseGet(CartItem::new);
        if (item.getId() == null) {
            item.setCart(cart);
            item.setBook(book);
            item.setItemType(type);
            item.setQuantity(quantity);
        } else {
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > available) throw new IllegalArgumentException("Số lượng trong giỏ vượt quá tồn kho");
            item.setQuantity(newQuantity);
        }
        item.setRentalDays(type == CartItemType.RENTAL ? rentalDays : null);
        cartItemRepository.save(item);
    }

    @Transactional
    public void update(User user, UUID itemId, int quantity, Integer rentalDays) {
        CartItem item = cartItemRepository.findByIdAndCart_User_Id(itemId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        long available = bookCopyRepository.countByBook_IdAndStatus(item.getBook().getId(), BookCopyStatus.AVAILABLE);
        if (quantity > available) throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        if (item.getItemType() == CartItemType.RENTAL) {
            if (rentalDays == null || rentalDays < 1 || rentalDays > 30) {
                throw new IllegalArgumentException("Số ngày thuê phải từ 1 đến 30");
            }
            item.setRentalDays(rentalDays);
        }
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void remove(User user, UUID itemId) {
        CartItem item = cartItemRepository.findByIdAndCart_User_Id(itemId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ"));
        cartItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<CartItem> itemsByType(User user, CartItemType type) {
        return getOrCreate(user).getItems().stream().filter(item -> item.getItemType() == type).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal purchaseSubtotal(User user) {
        return itemsByType(user, CartItemType.PURCHASE).stream()
                .map(item -> item.getBook().getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal rentalSubtotal(User user) {
        return itemsByType(user, CartItemType.RENTAL).stream()
                .map(item -> item.getBook().getRentalPricePerDay()
                        .multiply(BigDecimal.valueOf(item.getRentalDays()))
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal rentalDepositTotal(User user) {
        return itemsByType(user, CartItemType.RENTAL).stream()
                .map(item -> item.getBook().getRentalDeposit()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void clearByType(User user, CartItemType type) {
        Cart cart = getOrCreate(user);
        List<CartItem> toDelete = cart.getItems().stream().filter(item -> item.getItemType() == type).toList();
        cartItemRepository.deleteAll(toDelete);
    }
}

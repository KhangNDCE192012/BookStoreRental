/*
 Book Store & Rental Management System
 SQL Server 2019+ / SQL Server Management Studio 20
 Execute the complete script in a new query window.

 Sample accounts (BCrypt cost 12):
   admin    / Admin@123
   staff    / Staff@123
   customer / Customer@123
*/

USE master;
GO

IF DB_ID(N'BookStoreRentalDB') IS NOT NULL
BEGIN
    ALTER DATABASE BookStoreRentalDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE BookStoreRentalDB;
END;
GO

CREATE DATABASE BookStoreRentalDB;
GO

ALTER DATABASE BookStoreRentalDB SET READ_COMMITTED_SNAPSHOT ON;
GO

USE BookStoreRentalDB;
GO

/* =========================================================
   1. IDENTITY AND USER MANAGEMENT
   ========================================================= */
CREATE TABLE dbo.roles (
    role_id BIGINT IDENTITY(1,1) NOT NULL,
    name VARCHAR(30) NOT NULL,
    description NVARCHAR(255) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_name CHECK (name IN ('CUSTOMER','STAFF','ADMIN'))
);
GO

CREATE TABLE dbo.users (
    user_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_users_id DEFAULT NEWID(),
    username NVARCHAR(50) NOT NULL,
    email NVARCHAR(150) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name NVARCHAR(120) NOT NULL,
    phone NVARCHAR(20) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT df_users_status DEFAULT 'ACTIVE',
    role_id BIGINT NOT NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_users_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_users_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE','LOCKED','INACTIVE')),
    CONSTRAINT ck_users_username CHECK (LEN(LTRIM(RTRIM(username))) BETWEEN 4 AND 50),
    CONSTRAINT ck_users_email CHECK (email LIKE '%_@_%._%')
);
GO

CREATE TABLE dbo.staff (
    staff_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_staff_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    employee_code NVARCHAR(30) NOT NULL,
    position NVARCHAR(80) NULL,
    hire_date DATE NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_staff_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_staff_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_staff PRIMARY KEY (staff_id),
    CONSTRAINT uq_staff_user UNIQUE (user_id),
    CONSTRAINT uq_staff_employee_code UNIQUE (employee_code),
    CONSTRAINT fk_staff_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

CREATE TABLE dbo.addresses (
    address_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_addresses_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    receiver_name NVARCHAR(120) NOT NULL,
    receiver_phone NVARCHAR(20) NOT NULL,
    address_line NVARCHAR(255) NOT NULL,
    province NVARCHAR(100) NOT NULL,
    district NVARCHAR(100) NOT NULL,
    ward NVARCHAR(100) NOT NULL,
    is_default BIT NOT NULL CONSTRAINT df_addresses_default DEFAULT 0,
    active BIT NOT NULL CONSTRAINT df_addresses_active DEFAULT 1,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_addresses_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_addresses_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_addresses PRIMARY KEY (address_id),
    CONSTRAINT fk_addresses_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

CREATE TABLE dbo.password_reset_tokens (
    password_reset_token_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_password_reset_tokens_id DEFAULT NEWID(),
    token VARCHAR(100) NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    expires_at DATETIME2(6) NOT NULL,
    used BIT NOT NULL CONSTRAINT df_password_reset_tokens_used DEFAULT 0,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_password_reset_tokens_created DEFAULT SYSDATETIME(),
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (password_reset_token_id),
    CONSTRAINT uq_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_tokens_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

/* =========================================================
   2. BOOK CATALOG AND PHYSICAL INVENTORY
   ========================================================= */
CREATE TABLE dbo.categories (
    category_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_categories_id DEFAULT NEWID(),
    name NVARCHAR(120) NOT NULL,
    description NVARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT df_categories_status DEFAULT 'ACTIVE',
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_categories_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_categories_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE','INACTIVE'))
);
GO

CREATE TABLE dbo.books (
    book_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_books_id DEFAULT NEWID(),
    isbn NVARCHAR(20) NOT NULL,
    title NVARCHAR(250) NOT NULL,
    author NVARCHAR(180) NOT NULL,
    publisher NVARCHAR(180) NULL,
    publication_year INT NULL,
    language NVARCHAR(50) NULL,
    page_count INT NULL,
    description NVARCHAR(MAX) NULL,
    cover_image NVARCHAR(500) NULL,
    purchase_price DECIMAL(18,2) NOT NULL,
    rental_price_per_day DECIMAL(18,2) NOT NULL,
    rental_deposit DECIMAL(18,2) NOT NULL,
    category_id UNIQUEIDENTIFIER NOT NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT df_books_status DEFAULT 'ACTIVE',
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_books_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_books_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_books PRIMARY KEY (book_id),
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT fk_books_categories FOREIGN KEY (category_id) REFERENCES dbo.categories(category_id),
    CONSTRAINT ck_books_status CHECK (status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT ck_books_prices CHECK (purchase_price >= 0 AND rental_price_per_day >= 0 AND rental_deposit >= 0),
    CONSTRAINT ck_books_publication_year CHECK (publication_year IS NULL OR publication_year BETWEEN 1000 AND 2100),
    CONSTRAINT ck_books_page_count CHECK (page_count IS NULL OR page_count > 0)
);
GO

CREATE TABLE dbo.book_copies (
    book_copy_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_book_copies_id DEFAULT NEWID(),
    copy_code NVARCHAR(50) NOT NULL,
    book_id UNIQUEIDENTIFIER NOT NULL,
    book_condition VARCHAR(20) NOT NULL CONSTRAINT df_book_copies_condition DEFAULT 'NEW',
    status VARCHAR(20) NOT NULL CONSTRAINT df_book_copies_status DEFAULT 'AVAILABLE',
    shelf_location NVARCHAR(100) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_book_copies_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_book_copies_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_book_copies PRIMARY KEY (book_copy_id),
    CONSTRAINT uq_book_copies_code UNIQUE (copy_code),
    CONSTRAINT fk_book_copies_books FOREIGN KEY (book_id) REFERENCES dbo.books(book_id),
    CONSTRAINT ck_book_copies_condition CHECK (book_condition IN ('NEW','GOOD','FAIR','DAMAGED')),
    CONSTRAINT ck_book_copies_status CHECK (status IN ('AVAILABLE','RESERVED','SOLD','RENTED','MAINTENANCE','DAMAGED','LOST','INACTIVE'))
);
GO

/* =========================================================
   3. CART
   ========================================================= */
CREATE TABLE dbo.carts (
    cart_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_carts_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_carts_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_carts_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_carts PRIMARY KEY (cart_id),
    CONSTRAINT uq_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

CREATE TABLE dbo.cart_items (
    cart_item_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_cart_items_id DEFAULT NEWID(),
    cart_id UNIQUEIDENTIFIER NOT NULL,
    book_id UNIQUEIDENTIFIER NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CONSTRAINT df_cart_items_quantity DEFAULT 1,
    rental_days INT NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_cart_items_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_cart_items_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_cart_items PRIMARY KEY (cart_item_id),
    CONSTRAINT uq_cart_items_cart_book_type UNIQUE (cart_id, book_id, item_type),
    CONSTRAINT fk_cart_items_carts FOREIGN KEY (cart_id) REFERENCES dbo.carts(cart_id),
    CONSTRAINT fk_cart_items_books FOREIGN KEY (book_id) REFERENCES dbo.books(book_id),
    CONSTRAINT ck_cart_items_type CHECK (item_type IN ('PURCHASE','RENTAL')),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity BETWEEN 1 AND 20),
    CONSTRAINT ck_cart_items_rental_days CHECK (
        (item_type = 'PURCHASE' AND rental_days IS NULL)
        OR (item_type = 'RENTAL' AND rental_days BETWEEN 1 AND 30)
    )
);
GO

/* =========================================================
   4. VOUCHERS
   ========================================================= */
CREATE TABLE dbo.vouchers (
    voucher_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_vouchers_id DEFAULT NEWID(),
    code NVARCHAR(50) NOT NULL,
    name NVARCHAR(150) NOT NULL,
    voucher_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(18,2) NOT NULL,
    minimum_order_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_vouchers_minimum DEFAULT 0,
    maximum_discount DECIMAL(18,2) NULL,
    start_date DATETIME2(6) NOT NULL,
    end_date DATETIME2(6) NOT NULL,
    quantity INT NOT NULL,
    per_user_limit INT NOT NULL CONSTRAINT df_vouchers_user_limit DEFAULT 1,
    active BIT NOT NULL CONSTRAINT df_vouchers_active DEFAULT 1,
    created_by UNIQUEIDENTIFIER NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_vouchers_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_vouchers_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_vouchers PRIMARY KEY (voucher_id),
    CONSTRAINT uq_vouchers_code UNIQUE (code),
    CONSTRAINT fk_vouchers_users FOREIGN KEY (created_by) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_vouchers_type CHECK (voucher_type IN ('PERCENT','FIXED')),
    CONSTRAINT ck_vouchers_discount CHECK (
        discount_value >= 0 AND
        (voucher_type <> 'PERCENT' OR discount_value <= 100)
    ),
    CONSTRAINT ck_vouchers_amounts CHECK (minimum_order_amount >= 0 AND (maximum_discount IS NULL OR maximum_discount >= 0)),
    CONSTRAINT ck_vouchers_dates CHECK (end_date > start_date),
    CONSTRAINT ck_vouchers_quantity CHECK (quantity >= 0 AND per_user_limit > 0)
);
GO

CREATE TABLE dbo.user_vouchers (
    user_voucher_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_user_vouchers_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    voucher_id UNIQUEIDENTIFIER NOT NULL,
    usage_count INT NOT NULL CONSTRAINT df_user_vouchers_usage DEFAULT 0,
    last_used_at DATETIME2(6) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_user_vouchers_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_user_vouchers_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_user_vouchers PRIMARY KEY (user_voucher_id),
    CONSTRAINT uq_user_vouchers_user_voucher UNIQUE (user_id, voucher_id),
    CONSTRAINT fk_user_vouchers_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT fk_user_vouchers_vouchers FOREIGN KEY (voucher_id) REFERENCES dbo.vouchers(voucher_id),
    CONSTRAINT ck_user_vouchers_usage CHECK (usage_count >= 0)
);
GO

/* =========================================================
   5. WALLETS AND TRANSACTIONS
   ========================================================= */
CREATE TABLE dbo.wallets (
    wallet_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_wallets_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    balance DECIMAL(18,2) NOT NULL CONSTRAINT df_wallets_balance DEFAULT 0,
    version BIGINT NOT NULL CONSTRAINT df_wallets_version DEFAULT 0,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_wallets_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_wallets_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_wallets PRIMARY KEY (wallet_id),
    CONSTRAINT uq_wallets_user UNIQUE (user_id),
    CONSTRAINT fk_wallets_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_wallets_balance CHECK (balance >= 0)
);
GO

CREATE TABLE dbo.wallet_transactions (
    wallet_transaction_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_wallet_transactions_id DEFAULT NEWID(),
    wallet_id UNIQUEIDENTIFIER NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    balance_after DECIMAL(18,2) NOT NULL,
    reference_type NVARCHAR(50) NULL,
    reference_id NVARCHAR(50) NULL,
    description NVARCHAR(500) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_wallet_transactions_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_wallet_transactions PRIMARY KEY (wallet_transaction_id),
    CONSTRAINT fk_wallet_transactions_wallets FOREIGN KEY (wallet_id) REFERENCES dbo.wallets(wallet_id),
    CONSTRAINT ck_wallet_transactions_type CHECK (transaction_type IN (
        'TOP_UP','PURCHASE_PAYMENT','RENTAL_PAYMENT','REFUND','DEPOSIT_REFUND',
        'LATE_FEE','DAMAGE_FEE','LOST_FEE','EXTENSION_FEE','ADJUSTMENT'
    )),
    CONSTRAINT ck_wallet_transactions_balance CHECK (balance_after >= 0)
);
GO

/* =========================================================
   6. PURCHASE ORDERS
   ========================================================= */
CREATE TABLE dbo.purchase_orders (
    purchase_order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_purchase_orders_id DEFAULT NEWID(),
    order_code NVARCHAR(30) NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    voucher_id UNIQUEIDENTIFIER NULL,
    processed_by UNIQUEIDENTIFIER NULL,
    receiver_name NVARCHAR(120) NOT NULL,
    receiver_phone NVARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    shipping_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_purchase_orders_shipping DEFAULT 0,
    subtotal DECIMAL(18,2) NOT NULL CONSTRAINT df_purchase_orders_subtotal DEFAULT 0,
    discount_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_purchase_orders_discount DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_purchase_orders_total DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    order_status VARCHAR(30) NOT NULL,
    cancel_reason NVARCHAR(500) NULL,
    completed_at DATETIME2(6) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_purchase_orders_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_purchase_orders_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_purchase_orders PRIMARY KEY (purchase_order_id),
    CONSTRAINT uq_purchase_orders_code UNIQUE (order_code),
    CONSTRAINT fk_purchase_orders_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT fk_purchase_orders_vouchers FOREIGN KEY (voucher_id) REFERENCES dbo.vouchers(voucher_id),
    CONSTRAINT fk_purchase_orders_staff FOREIGN KEY (processed_by) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_purchase_orders_amounts CHECK (
        shipping_fee >= 0 AND subtotal >= 0 AND discount_amount >= 0 AND total_amount >= 0
    ),
    CONSTRAINT ck_purchase_orders_payment_method CHECK (payment_method IN ('WALLET','CASH_ON_DELIVERY','BANK_TRANSFER')),
    CONSTRAINT ck_purchase_orders_payment_status CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED','PARTIALLY_REFUNDED')),
    CONSTRAINT ck_purchase_orders_status CHECK (order_status IN ('PENDING','CONFIRMED','PROCESSING','SHIPPING','COMPLETED','CANCELLED','REJECTED'))
);
GO

CREATE TABLE dbo.purchase_order_details (
    purchase_order_detail_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_purchase_order_details_id DEFAULT NEWID(),
    purchase_order_id UNIQUEIDENTIFIER NOT NULL,
    book_id UNIQUEIDENTIFIER NOT NULL,
    book_copy_id UNIQUEIDENTIFIER NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    quantity INT NOT NULL CONSTRAINT df_purchase_order_details_quantity DEFAULT 1,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_purchase_order_details_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_purchase_order_details_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_purchase_order_details PRIMARY KEY (purchase_order_detail_id),
    CONSTRAINT uq_purchase_order_details_copy UNIQUE (book_copy_id),
    CONSTRAINT fk_purchase_order_details_orders FOREIGN KEY (purchase_order_id) REFERENCES dbo.purchase_orders(purchase_order_id),
    CONSTRAINT fk_purchase_order_details_books FOREIGN KEY (book_id) REFERENCES dbo.books(book_id),
    CONSTRAINT fk_purchase_order_details_copies FOREIGN KEY (book_copy_id) REFERENCES dbo.book_copies(book_copy_id),
    CONSTRAINT ck_purchase_order_details_values CHECK (unit_price >= 0 AND quantity > 0)
);
GO

/* =========================================================
   7. RENTAL, EXTENSION AND RETURN
   ========================================================= */
CREATE TABLE dbo.rental_orders (
    rental_order_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_rental_orders_id DEFAULT NEWID(),
    rental_code NVARCHAR(30) NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    voucher_id UNIQUEIDENTIFIER NULL,
    processed_by UNIQUEIDENTIFIER NULL,
    receiver_name NVARCHAR(120) NOT NULL,
    receiver_phone NVARCHAR(20) NOT NULL,
    shipping_address NVARCHAR(500) NOT NULL,
    rental_date DATETIME2(6) NULL,
    due_date DATETIME2(6) NULL,
    return_date DATETIME2(6) NULL,
    rental_days INT NOT NULL,
    deposit_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_deposit DEFAULT 0,
    rental_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_fee DEFAULT 0,
    late_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_late DEFAULT 0,
    damage_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_damage DEFAULT 0,
    discount_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_discount DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_orders_total DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    rental_status VARCHAR(30) NOT NULL,
    cancel_reason NVARCHAR(500) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_rental_orders_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_rental_orders_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_rental_orders PRIMARY KEY (rental_order_id),
    CONSTRAINT uq_rental_orders_code UNIQUE (rental_code),
    CONSTRAINT fk_rental_orders_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT fk_rental_orders_vouchers FOREIGN KEY (voucher_id) REFERENCES dbo.vouchers(voucher_id),
    CONSTRAINT fk_rental_orders_staff FOREIGN KEY (processed_by) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_rental_orders_days CHECK (rental_days BETWEEN 1 AND 30),
    CONSTRAINT ck_rental_orders_amounts CHECK (
        deposit_amount >= 0 AND rental_fee >= 0 AND late_fee >= 0 AND
        damage_fee >= 0 AND discount_amount >= 0 AND total_amount >= 0
    ),
    CONSTRAINT ck_rental_orders_payment_method CHECK (payment_method IN ('WALLET','CASH_ON_DELIVERY','BANK_TRANSFER')),
    CONSTRAINT ck_rental_orders_payment_status CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED','PARTIALLY_REFUNDED')),
    CONSTRAINT ck_rental_orders_status CHECK (rental_status IN ('PENDING','CONFIRMED','RENTING','RETURN_REQUESTED','RETURNED','OVERDUE','CANCELLED','REJECTED','LOST'))
);
GO

CREATE TABLE dbo.rental_order_details (
    rental_order_detail_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_rental_order_details_id DEFAULT NEWID(),
    rental_order_id UNIQUEIDENTIFIER NOT NULL,
    book_id UNIQUEIDENTIFIER NOT NULL,
    book_copy_id UNIQUEIDENTIFIER NOT NULL,
    rental_days INT NOT NULL,
    rental_date DATETIME2(6) NULL,
    due_date DATETIME2(6) NULL,
    return_date DATETIME2(6) NULL,
    rental_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_order_details_fee DEFAULT 0,
    deposit_amount DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_order_details_deposit DEFAULT 0,
    late_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_order_details_late DEFAULT 0,
    damage_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_rental_order_details_damage DEFAULT 0,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_rental_order_details_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_rental_order_details_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_rental_order_details PRIMARY KEY (rental_order_detail_id),
    CONSTRAINT fk_rental_order_details_orders FOREIGN KEY (rental_order_id) REFERENCES dbo.rental_orders(rental_order_id),
    CONSTRAINT fk_rental_order_details_books FOREIGN KEY (book_id) REFERENCES dbo.books(book_id),
    CONSTRAINT fk_rental_order_details_copies FOREIGN KEY (book_copy_id) REFERENCES dbo.book_copies(book_copy_id),
    CONSTRAINT ck_rental_order_details_days CHECK (rental_days > 0),
    CONSTRAINT ck_rental_order_details_amounts CHECK (rental_fee >= 0 AND deposit_amount >= 0 AND late_fee >= 0 AND damage_fee >= 0)
);
GO

CREATE TABLE dbo.book_extension_requests (
    extension_request_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_extension_requests_id DEFAULT NEWID(),
    rental_order_detail_id UNIQUEIDENTIFIER NOT NULL,
    extra_days INT NOT NULL,
    requested_due_date DATETIME2(6) NOT NULL,
    extra_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_extension_requests_fee DEFAULT 0,
    reason NVARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL CONSTRAINT df_extension_requests_status DEFAULT 'PENDING',
    processed_by UNIQUEIDENTIFIER NULL,
    processed_at DATETIME2(6) NULL,
    staff_note NVARCHAR(500) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_extension_requests_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_extension_requests_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_book_extension_requests PRIMARY KEY (extension_request_id),
    CONSTRAINT fk_extension_requests_rental_details FOREIGN KEY (rental_order_detail_id) REFERENCES dbo.rental_order_details(rental_order_detail_id),
    CONSTRAINT fk_extension_requests_users FOREIGN KEY (processed_by) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_extension_requests_days CHECK (extra_days BETWEEN 1 AND 14),
    CONSTRAINT ck_extension_requests_fee CHECK (extra_fee >= 0),
    CONSTRAINT ck_extension_requests_status CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))
);
GO

CREATE TABLE dbo.book_return_records (
    book_return_record_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_return_records_id DEFAULT NEWID(),
    rental_order_detail_id UNIQUEIDENTIFIER NOT NULL,
    returned_at DATETIME2(6) NOT NULL,
    condition_before VARCHAR(20) NOT NULL,
    condition_after VARCHAR(20) NOT NULL,
    late_days INT NOT NULL CONSTRAINT df_return_records_late_days DEFAULT 0,
    late_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_return_records_late_fee DEFAULT 0,
    damage_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_return_records_damage_fee DEFAULT 0,
    lost_fee DECIMAL(18,2) NOT NULL CONSTRAINT df_return_records_lost_fee DEFAULT 0,
    deposit_refund DECIMAL(18,2) NOT NULL CONSTRAINT df_return_records_deposit_refund DEFAULT 0,
    notes NVARCHAR(1000) NULL,
    processed_by UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_return_records_created_at DEFAULT SYSDATETIME(),
    updated_at DATETIME2(6) NOT NULL CONSTRAINT df_return_records_updated_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_book_return_records PRIMARY KEY (book_return_record_id),
    CONSTRAINT uq_return_record_detail UNIQUE (rental_order_detail_id),
    CONSTRAINT fk_return_records_rental_details FOREIGN KEY (rental_order_detail_id) REFERENCES dbo.rental_order_details(rental_order_detail_id),
    CONSTRAINT fk_return_records_users FOREIGN KEY (processed_by) REFERENCES dbo.users(user_id),
    CONSTRAINT ck_return_records_condition_before CHECK (condition_before IN ('NEW','GOOD','FAIR','DAMAGED')),
    CONSTRAINT ck_return_records_condition_after CHECK (condition_after IN ('GOOD','FAIR','DAMAGED','LOST')),
    CONSTRAINT ck_return_records_values CHECK (late_days >= 0 AND late_fee >= 0 AND damage_fee >= 0 AND lost_fee >= 0 AND deposit_refund >= 0)
);
GO

/* =========================================================
   8. PAYMENTS AND AUDIT LOG
   ========================================================= */
CREATE TABLE dbo.payments (
    payment_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_payments_id DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    purchase_order_id UNIQUEIDENTIFIER NULL,
    rental_order_id UNIQUEIDENTIFIER NULL,
    amount DECIMAL(18,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    provider_reference NVARCHAR(100) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_payments_created_at DEFAULT SYSDATETIME(),
    paid_at DATETIME2(6) NULL,
    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT uq_payments_purchase_order UNIQUE (purchase_order_id),
    CONSTRAINT uq_payments_rental_order UNIQUE (rental_order_id),
    CONSTRAINT fk_payments_users FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT fk_payments_purchase_orders FOREIGN KEY (purchase_order_id) REFERENCES dbo.purchase_orders(purchase_order_id),
    CONSTRAINT fk_payments_rental_orders FOREIGN KEY (rental_order_id) REFERENCES dbo.rental_orders(rental_order_id),
    CONSTRAINT ck_payments_amount CHECK (amount >= 0),
    CONSTRAINT ck_payments_target CHECK (
        (purchase_order_id IS NOT NULL AND rental_order_id IS NULL)
        OR (purchase_order_id IS NULL AND rental_order_id IS NOT NULL)
    ),
    CONSTRAINT ck_payments_method CHECK (payment_method IN ('WALLET','CASH_ON_DELIVERY','BANK_TRANSFER')),
    CONSTRAINT ck_payments_status CHECK (payment_status IN ('PENDING','SUCCESS','FAILED','REFUNDED','PARTIALLY_REFUNDED'))
);
GO

CREATE TABLE dbo.activity_logs (
    activity_log_id UNIQUEIDENTIFIER NOT NULL CONSTRAINT df_activity_logs_id DEFAULT NEWID(),
    actor_id UNIQUEIDENTIFIER NULL,
    action NVARCHAR(100) NOT NULL,
    entity_type NVARCHAR(100) NULL,
    entity_id NVARCHAR(100) NULL,
    details NVARCHAR(2000) NULL,
    ip_address NVARCHAR(50) NULL,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_activity_logs_created_at DEFAULT SYSDATETIME(),
    CONSTRAINT pk_activity_logs PRIMARY KEY (activity_log_id),
    CONSTRAINT fk_activity_logs_users FOREIGN KEY (actor_id) REFERENCES dbo.users(user_id)
);
GO

/* =========================================================
   9. INDEXES FOR SEARCH AND PROCESSING
   ========================================================= */
CREATE INDEX ix_users_role_id ON dbo.users(role_id);
CREATE INDEX ix_users_status ON dbo.users(status);
CREATE INDEX ix_addresses_user_id ON dbo.addresses(user_id);
CREATE INDEX ix_password_reset_tokens_user_expiry ON dbo.password_reset_tokens(user_id, expires_at);
CREATE INDEX ix_books_title ON dbo.books(title);
CREATE INDEX ix_books_author ON dbo.books(author);
CREATE INDEX ix_books_category_id ON dbo.books(category_id);
CREATE INDEX ix_books_status ON dbo.books(status);
CREATE INDEX ix_book_copies_book_status ON dbo.book_copies(book_id, status);
CREATE INDEX ix_cart_items_cart_id ON dbo.cart_items(cart_id);
CREATE INDEX ix_vouchers_active_dates ON dbo.vouchers(active, start_date, end_date);
CREATE INDEX ix_wallet_transactions_wallet_id ON dbo.wallet_transactions(wallet_id);
CREATE INDEX ix_wallet_transactions_created_at ON dbo.wallet_transactions(created_at);
CREATE INDEX ix_purchase_orders_user_id ON dbo.purchase_orders(user_id);
CREATE INDEX ix_purchase_orders_status ON dbo.purchase_orders(order_status);
CREATE INDEX ix_purchase_orders_created_at ON dbo.purchase_orders(created_at);
CREATE INDEX ix_purchase_order_details_order_id ON dbo.purchase_order_details(purchase_order_id);
CREATE INDEX ix_purchase_order_details_copy_id ON dbo.purchase_order_details(book_copy_id);
CREATE INDEX ix_rental_orders_user_id ON dbo.rental_orders(user_id);
CREATE INDEX ix_rental_orders_status ON dbo.rental_orders(rental_status);
CREATE INDEX ix_rental_orders_due_date ON dbo.rental_orders(due_date);
CREATE INDEX ix_rental_order_details_order_id ON dbo.rental_order_details(rental_order_id);
CREATE INDEX ix_rental_order_details_copy_id ON dbo.rental_order_details(book_copy_id);
CREATE INDEX ix_extension_requests_status ON dbo.book_extension_requests(status);
CREATE INDEX ix_payments_user_id ON dbo.payments(user_id);
CREATE INDEX ix_payments_status_created ON dbo.payments(payment_status, created_at);
CREATE INDEX ix_activity_logs_actor_id ON dbo.activity_logs(actor_id);
CREATE INDEX ix_activity_logs_created_at ON dbo.activity_logs(created_at);
CREATE INDEX ix_activity_logs_entity ON dbo.activity_logs(entity_type, entity_id);
GO

/* Only one active default address is permitted per user. */
CREATE UNIQUE INDEX ux_addresses_one_default
ON dbo.addresses(user_id)
WHERE is_default = 1 AND active = 1;
GO

/* Only one pending extension request per rental detail. */
CREATE UNIQUE INDEX ux_extension_one_pending
ON dbo.book_extension_requests(rental_order_detail_id)
WHERE status = 'PENDING';
GO

/* =========================================================
   10. SAMPLE DATA
   ========================================================= */
INSERT INTO dbo.roles(name, description) VALUES
('CUSTOMER', N'Khách hàng mua và thuê sách'),
('STAFF', N'Nhân viên xử lý đơn hàng, thuê trả và doanh thu'),
('ADMIN', N'Quản trị toàn bộ hệ thống');
GO

DECLARE @customerRole BIGINT = (SELECT role_id FROM dbo.roles WHERE name = 'CUSTOMER');
DECLARE @staffRole BIGINT = (SELECT role_id FROM dbo.roles WHERE name = 'STAFF');
DECLARE @adminRole BIGINT = (SELECT role_id FROM dbo.roles WHERE name = 'ADMIN');

DECLARE @adminId UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000001';
DECLARE @staffId UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000002';
DECLARE @customerId UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000003';

INSERT INTO dbo.users(user_id, username, email, password_hash, full_name, phone, status, role_id, created_at, updated_at) VALUES
(@adminId, N'admin', N'admin@bookstore.local', '$2y$12$N4ZytGBF8tcUq.N1SLFc6efr1Xtg6JwS1S8m64icKoaYlkcfBl3Ju', N'Quản trị hệ thống', N'0900000001', 'ACTIVE', @adminRole, SYSDATETIME(), SYSDATETIME()),
(@staffId, N'staff', N'staff@bookstore.local', '$2y$12$gG1GikqQAEFfRBOjtgtGYO5vws4jch14O40oqN2.YyuV0OSf24kZW', N'Nhân viên cửa hàng', N'0900000002', 'ACTIVE', @staffRole, SYSDATETIME(), SYSDATETIME()),
(@customerId, N'customer', N'customer@bookstore.local', '$2y$12$Lw.97ntcWo7CHfg4nJgJ5OmJLeEdL8TuBiM5JN1nFYhTOn/DkwQ1K', N'Khách hàng mẫu', N'0900000003', 'ACTIVE', @customerRole, SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.staff(staff_id, user_id, employee_code, position, hire_date, created_at, updated_at)
VALUES ('10000000-0000-0000-0000-000000000002', @staffId, N'STAFF001', N'Nhân viên vận hành', CAST(GETDATE() AS DATE), SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.addresses(address_id, user_id, receiver_name, receiver_phone, address_line, province, district, ward, is_default, active, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000003', @customerId, N'Khách hàng mẫu', N'0900000003', N'Số 1, đường Nguyễn Văn Cừ', N'Thành phố Cần Thơ', N'Quận Ninh Kiều', N'Phường An Khánh', 1, 1, SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.wallets(wallet_id, user_id, balance, version, created_at, updated_at) VALUES
('30000000-0000-0000-0000-000000000001', @adminId, 0, 0, SYSDATETIME(), SYSDATETIME()),
('30000000-0000-0000-0000-000000000002', @staffId, 0, 0, SYSDATETIME(), SYSDATETIME()),
('30000000-0000-0000-0000-000000000003', @customerId, 5000000, 0, SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.carts(cart_id, user_id, created_at, updated_at) VALUES
('40000000-0000-0000-0000-000000000001', @adminId, SYSDATETIME(), SYSDATETIME()),
('40000000-0000-0000-0000-000000000002', @staffId, SYSDATETIME(), SYSDATETIME()),
('40000000-0000-0000-0000-000000000003', @customerId, SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.wallet_transactions(wallet_transaction_id, wallet_id, transaction_type, amount, balance_after, reference_type, description, created_at)
VALUES ('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 'TOP_UP', 5000000, 5000000, N'SEED', N'Số dư ví dữ liệu mẫu', SYSDATETIME());

DECLARE @catLiterature UNIQUEIDENTIFIER = '60000000-0000-0000-0000-000000000001';
DECLARE @catBusiness UNIQUEIDENTIFIER = '60000000-0000-0000-0000-000000000002';
DECLARE @catTechnology UNIQUEIDENTIFIER = '60000000-0000-0000-0000-000000000003';
DECLARE @catChildren UNIQUEIDENTIFIER = '60000000-0000-0000-0000-000000000004';

INSERT INTO dbo.categories(category_id, name, description, status, created_at, updated_at) VALUES
(@catLiterature, N'Văn học', N'Tiểu thuyết, truyện ngắn và tác phẩm văn học Việt Nam, thế giới', 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@catBusiness, N'Kinh tế - Kinh doanh', N'Quản trị, tài chính, khởi nghiệp và kỹ năng kinh doanh', 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@catTechnology, N'Công nghệ thông tin', N'Lập trình, cơ sở dữ liệu, phần mềm và công nghệ', 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@catChildren, N'Thiếu nhi', N'Sách truyện và kiến thức dành cho thiếu nhi', 'ACTIVE', SYSDATETIME(), SYSDATETIME());

DECLARE @book1 UNIQUEIDENTIFIER = '70000000-0000-0000-0000-000000000001';
DECLARE @book2 UNIQUEIDENTIFIER = '70000000-0000-0000-0000-000000000002';
DECLARE @book3 UNIQUEIDENTIFIER = '70000000-0000-0000-0000-000000000003';
DECLARE @book4 UNIQUEIDENTIFIER = '70000000-0000-0000-0000-000000000004';

INSERT INTO dbo.books(book_id, isbn, title, author, publisher, publication_year, language, page_count, description, cover_image, purchase_price, rental_price_per_day, rental_deposit, category_id, status, created_at, updated_at) VALUES
(@book1, N'9786042113743', N'Dế Mèn Phiêu Lưu Ký', N'Tô Hoài', N'NXB Kim Đồng', 2023, N'Tiếng Việt', 192, N'Tác phẩm văn học thiếu nhi kinh điển kể về hành trình trưởng thành của Dế Mèn.', NULL, 85000, 5000, 50000, @catChildren, 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@book2, N'9786043949839', N'Nhà Giả Kim', N'Paulo Coelho', N'NXB Hội Nhà Văn', 2022, N'Tiếng Việt', 228, N'Câu chuyện về hành trình theo đuổi ước mơ và lắng nghe tiếng gọi của trái tim.', NULL, 79000, 5000, 50000, @catLiterature, 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@book3, N'9780132350884', N'Clean Code', N'Robert C. Martin', N'Prentice Hall', 2008, N'English', 464, N'Tài liệu nền tảng về cách viết mã sạch, dễ đọc và dễ bảo trì.', NULL, 450000, 25000, 300000, @catTechnology, 'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(@book4, N'9786043659219', N'Khởi Nghiệp Tinh Gọn', N'Eric Ries', N'NXB Tổng hợp TP.HCM', 2021, N'Tiếng Việt', 336, N'Phương pháp phát triển sản phẩm và doanh nghiệp bằng thử nghiệm, đo lường và học hỏi.', NULL, 165000, 10000, 100000, @catBusiness, 'ACTIVE', SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.book_copies(book_copy_id, copy_code, book_id, book_condition, status, shelf_location, created_at, updated_at) VALUES
('80000000-0000-0000-0000-000000000001', N'DM-001', @book1, 'NEW', 'AVAILABLE', N'A1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000002', N'DM-002', @book1, 'GOOD', 'AVAILABLE', N'A1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000003', N'DM-003', @book1, 'GOOD', 'AVAILABLE', N'A1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000004', N'NGK-001', @book2, 'NEW', 'AVAILABLE', N'A1-02', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000005', N'NGK-002', @book2, 'GOOD', 'AVAILABLE', N'A1-02', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000006', N'CC-001', @book3, 'NEW', 'AVAILABLE', N'T1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000007', N'CC-002', @book3, 'GOOD', 'AVAILABLE', N'T1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000008', N'KN-001', @book4, 'NEW', 'AVAILABLE', N'B1-01', SYSDATETIME(), SYSDATETIME()),
('80000000-0000-0000-0000-000000000009', N'KN-002', @book4, 'GOOD', 'AVAILABLE', N'B1-01', SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.vouchers(voucher_id, code, name, voucher_type, discount_value, minimum_order_amount, maximum_discount, start_date, end_date, quantity, per_user_limit, active, created_by, created_at, updated_at)
VALUES
('90000000-0000-0000-0000-000000000001', N'WELCOME10', N'Giảm 10% cho khách hàng', 'PERCENT', 10, 100000, 50000, DATEADD(DAY,-30,SYSDATETIME()), DATEADD(YEAR,1,SYSDATETIME()), 100, 2, 1, @adminId, SYSDATETIME(), SYSDATETIME()),
('90000000-0000-0000-0000-000000000002', N'BOOK30K', N'Giảm trực tiếp 30.000 đồng', 'FIXED', 30000, 200000, NULL, DATEADD(DAY,-30,SYSDATETIME()), DATEADD(YEAR,1,SYSDATETIME()), 50, 1, 1, @adminId, SYSDATETIME(), SYSDATETIME());

INSERT INTO dbo.activity_logs(activity_log_id, actor_id, action, entity_type, entity_id, details, created_at)
VALUES ('A0000000-0000-0000-0000-000000000001', @adminId, N'INITIALIZE_DATABASE', N'System', N'BookStoreRentalDB', N'Khởi tạo dữ liệu mẫu Book Store & Rental', SYSDATETIME());
GO

/* =========================================================
   11. VERIFICATION QUERIES
   ========================================================= */
SELECT r.role_id, r.name, COUNT(u.user_id) AS user_count
FROM dbo.roles r
LEFT JOIN dbo.users u ON u.role_id = r.role_id
GROUP BY r.role_id, r.name
ORDER BY r.role_id;

SELECT b.isbn, b.title, b.author, c.name AS category,
       SUM(CASE WHEN bc.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies,
       COUNT(bc.book_copy_id) AS total_copies
FROM dbo.books b
JOIN dbo.categories c ON c.category_id = b.category_id
LEFT JOIN dbo.book_copies bc ON bc.book_id = b.book_id
GROUP BY b.isbn, b.title, b.author, c.name
ORDER BY b.title;

SELECT u.username, r.name AS role_name, u.status, w.balance
FROM dbo.users u
JOIN dbo.roles r ON r.role_id = u.role_id
LEFT JOIN dbo.wallets w ON w.user_id = u.user_id
ORDER BY r.role_id DESC, u.username;
GO

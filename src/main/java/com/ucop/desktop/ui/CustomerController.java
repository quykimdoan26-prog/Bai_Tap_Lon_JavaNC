package com.ucop.desktop.ui;

import com.ucop.desktop.domain.*;
import com.ucop.desktop.repo.ItemRepository;
import com.ucop.desktop.service.OrderService;
import com.ucop.desktop.service.RegistrationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerController {
    private final ItemRepository items;
    private final OrderService orders;
    private final RegistrationService registration;
    private final ApplicationContext context;
    private final ObservableList<CartRow> cart = FXCollections.observableArrayList();
    private Account currentAccount;

    @FXML private Label welcomeLabel, messageLabel, totalLabel;
    @FXML private TableView<Item> productTable;
    @FXML private TableColumn<Item, String> productSkuColumn, productNameColumn, productPriceColumn;
    @FXML private TableView<CartRow> cartTable;
    @FXML private TableColumn<CartRow, String> cartSkuColumn, cartNameColumn, cartQuantityColumn, cartTotalColumn;
    @FXML private TextField quantityField, promotionField, fullNameField, emailField, phoneField;
    @FXML private ComboBox<String> paymentMethodBox;
    @FXML private TableView<CustomerOrder> orderTable;
    @FXML private TableColumn<CustomerOrder, String> orderIdColumn, orderStatusColumn, orderTotalColumn;

    public CustomerController(ItemRepository items, OrderService orders, RegistrationService registration,
                              ApplicationContext context) {
        this.items = items; this.orders = orders; this.registration = registration; this.context = context;
    }

    public void setCurrentAccount(Account account) {
        currentAccount = account;
        welcomeLabel.setText("Xin chào, " + account.getProfile().getFullName());
        fullNameField.setText(account.getProfile().getFullName());
        emailField.setText(account.getProfile().getEmail());
        phoneField.setText(account.getProfile().getPhone());
        orderTable.setItems(FXCollections.observableArrayList(orders.ordersFor(account)));
    }

    @FXML
    void initialize() {
        productSkuColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSku()));
        productNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        productPriceColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getBasePrice())));
        cartSkuColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().item().getSku()));
        cartNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().item().getName()));
        cartQuantityColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().quantity())));
        cartTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().total())));
        orderIdColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrderId())));
        orderStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        orderTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getGrandTotal())));
        paymentMethodBox.setItems(FXCollections.observableArrayList("COD", "TRANSFER", "GATEWAY_SIMULATED", "WALLET"));
        paymentMethodBox.getSelectionModel().selectFirst();
        productTable.setItems(FXCollections.observableArrayList(items.findByStatusOrderByNameAsc("ACTIVE")));
        cartTable.setItems(cart);
    }

    @FXML
    void addToCart() {
        try {
            Item item = productTable.getSelectionModel().getSelectedItem();
            if (item == null) throw new IllegalArgumentException("Hãy chọn sản phẩm.");
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            for (int i = 0; i < cart.size(); i++) {
                if (cart.get(i).item().getItemId().equals(item.getItemId())) {
                    cart.set(i, new CartRow(item, cart.get(i).quantity() + quantity));
                    updateTotal(); return;
                }
            }
            cart.add(new CartRow(item, quantity));
            updateTotal();
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    void removeFromCart() {
        CartRow row = cartTable.getSelectionModel().getSelectedItem();
        if (row != null) { cart.remove(row); updateTotal(); }
    }

    @FXML
    void checkout() {
        try {
            throwIfCartEmpty();
            List<OrderService.CartLine> lines = new ArrayList<>();
            for (CartRow row : cart) lines.add(new OrderService.CartLine(row.item().getItemId(), row.quantity()));
            int warehouseId = orders.warehouseFor(lines);
            CustomerOrder order = orders.placeOrder(currentAccount, lines, warehouseId,
                    promotionField.getText(), paymentMethodBox.getValue());
            cart.clear(); updateTotal(); loadOrders();
            messageLabel.setText("Đặt hàng thành công. Mã đơn #" + order.getOrderId());
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    void updateProfile() {
        try {
            registration.updateProfile(currentAccount, fullNameField.getText(), emailField.getText(), phoneField.getText());
            welcomeLabel.setText("Xin chào, " + currentAccount.getProfile().getFullName());
            messageLabel.setText("Đã cập nhật thông tin cá nhân.");
        } catch (Exception ex) { messageLabel.setText(ex.getMessage()); }
    }

    @FXML
    void refreshOrders() { loadOrders(); }
    @FXML
    void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(context::getBean);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 520, 360));
            stage.setTitle("UCOP - Đăng nhập");
            stage.centerOnScreen();
        } catch (Exception ex) {
            messageLabel.setText("Không thể quay lại màn hình đăng nhập: " + ex.getMessage());
        }
    }

    private void loadOrders() { orderTable.setItems(FXCollections.observableArrayList(orders.ordersFor(currentAccount))); }
    private void throwIfCartEmpty() { if (cart.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống."); }
    private void updateTotal() {
        BigDecimal value = cart.stream().map(CartRow::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Tạm tính: " + format(value) + " VND");
    }
    private String format(BigDecimal value) { return String.format("%,.0f", value == null ? BigDecimal.ZERO : value); }
    public record CartRow(Item item, int quantity) {
        BigDecimal total() { return item.getBasePrice().multiply(BigDecimal.valueOf(quantity)); }
    }
}

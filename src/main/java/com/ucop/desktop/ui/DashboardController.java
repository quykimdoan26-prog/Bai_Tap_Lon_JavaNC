package com.ucop.desktop.ui;

import com.ucop.desktop.domain.*;
import com.ucop.desktop.repo.*;
import com.ucop.desktop.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

@Component
public class DashboardController {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final ItemRepository items;
    private final CategoryRepository categories;
    private final WarehouseRepository warehouses;
    private final StockItemRepository stocks;
    private final OrderRepository orders;
    private final CatalogService catalog;
    private final AccountService accountService;
    private final InventoryService inventory;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ReportService reports;
    private final PromotionService promotionService;
    private final AuthService authService;
    private final ApplicationContext context;

    private Account currentAccount;

    @FXML private Label welcomeLabel, todayRevenueLabel, resultLabel;
    @FXML private Tab catalogTab, accountTab, inventoryTab, operationsTab, promotionTab, customerTab;
    @FXML private BarChart<String, Number> revenueChart;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> skuColumn, nameColumn, priceColumn, statusColumn;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private ComboBox<Category> parentCategoryBox;
    @FXML private TextField skuField, nameField, priceField, categoryNameField;

    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, String> accountUserColumn, accountRoleColumn, accountStatusColumn;
    @FXML private TextField accountUsernameField, accountPasswordField;
    @FXML private ComboBox<Role> accountRoleBox;

    @FXML private TableView<StockItem> stockTable;
    @FXML private TableColumn<StockItem, String> stockItemColumn, stockWarehouseColumn, stockOnHandColumn, stockReservedColumn;
    @FXML private ComboBox<Item> stockItemBox;
    @FXML private ComboBox<Warehouse> stockWarehouseBox;
    @FXML private TextField stockOnHandField, stockReservedField, warehouseCodeField, warehouseNameField;

    @FXML private TableView<CustomerOrder> orderTable;
    @FXML private TableColumn<CustomerOrder, String> orderIdColumn, orderCustomerColumn, orderStatusColumn, orderTotalColumn;
    @FXML private TableView<CustomerOrder> customerOrderTable;
    @FXML private TableColumn<CustomerOrder, String> customerOrderIdColumn, customerOrderStatusColumn, customerOrderTotalColumn;
    @FXML private ComboBox<String> statusBox;
    @FXML private TextField refundAmountField;

    @FXML private ComboBox<Item> cartItemBox;
    @FXML private TextField cartQuantityField, promotionField;
    @FXML private ComboBox<String> paymentMethodBox;
    @FXML private TextField promotionCodeField, promotionValueField;
    @FXML private ComboBox<String> promotionTypeBox;
    @FXML private PasswordField oldPasswordField, newPasswordField;

    public DashboardController(AccountRepository accounts, RoleRepository roles, ItemRepository items,
                               CategoryRepository categories, WarehouseRepository warehouses,
                               StockItemRepository stocks, OrderRepository orders, CatalogService catalog,
                               AccountService accountService, InventoryService inventory, OrderService orderService,
                               PaymentService paymentService, ReportService reports, PromotionService promotionService,
                               AuthService authService, ApplicationContext context) {
        this.accounts = accounts; this.roles = roles; this.items = items; this.categories = categories;
        this.warehouses = warehouses; this.stocks = stocks; this.orders = orders; this.catalog = catalog;
        this.accountService = accountService; this.inventory = inventory; this.orderService = orderService;
        this.paymentService = paymentService; this.reports = reports;
        this.promotionService = promotionService; this.authService = authService; this.context = context;
    }

    public void setCurrentAccount(Account account) {
        currentAccount = account;
        if (welcomeLabel != null && account != null) {
            String role = account.getRole().getRoleName().toUpperCase();
            welcomeLabel.setText("Xin chào, " + account.getUsername() + " (" + role + ")");
            boolean admin = "ADMIN".equals(role);
            boolean staff = "STAFF".equals(role);
            catalogTab.setDisable(!admin && !staff);
            accountTab.setDisable(!admin);
            inventoryTab.setDisable(!admin && !staff);
            operationsTab.setDisable(!admin && !staff);
            promotionTab.setDisable(!admin);
            customerTab.setDisable(true);
            loadOrders();
        }
    }

    @FXML
    void initialize() {
        skuColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSku()));
        nameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        priceColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getBasePrice())));
        statusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        accountUserColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        accountRoleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().getRoleName()));
        accountStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        stockItemColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));
        stockWarehouseColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getWarehouse().getCode()));
        stockOnHandColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantityOnHand())));
        stockReservedColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantityReserved())));
        orderIdColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrderId())));
        orderCustomerColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccount().getUsername()));
        orderStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        orderTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getGrandTotal())));
        customerOrderIdColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getOrderId())));
        customerOrderStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        customerOrderTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(format(d.getValue().getGrandTotal())));
        statusBox.setItems(FXCollections.observableArrayList(
                "PAID", "PACKED", "SHIPPED", "DELIVERED", "CLOSED", "CANCELED", "RMA_REQUESTED", "REFUNDED"));
        paymentMethodBox.setItems(FXCollections.observableArrayList("COD", "TRANSFER", "GATEWAY_SIMULATED", "WALLET"));
        paymentMethodBox.getSelectionModel().selectFirst();
        promotionTypeBox.setItems(FXCollections.observableArrayList("PERCENTAGE", "FIXED_AMOUNT"));
        promotionTypeBox.getSelectionModel().selectFirst();
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, item) -> {
            if (item != null) {
                skuField.setText(item.getSku());
                nameField.setText(item.getName());
                priceField.setText(item.getBasePrice().toPlainString());
                categoryBox.setValue(item.getCategory());
            }
        });
        stockTable.getSelectionModel().selectedItemProperty().addListener((obs, oldStock, stock) -> {
            if (stock != null) {
                stockItemBox.setValue(stock.getItem());
                stockWarehouseBox.setValue(stock.getWarehouse());
                stockOnHandField.setText(String.valueOf(stock.getQuantityOnHand()));
                stockReservedField.setText(String.valueOf(stock.getQuantityReserved()));
            }
        });
        loadAll();
    }

    private void loadAll() {
        loadCatalog(); loadAccounts(); loadInventory(); loadOrders(); loadRevenue();
    }
    private void loadCatalog() {
        itemTable.setItems(FXCollections.observableArrayList(items.findByStatusOrderByNameAsc("ACTIVE")));
        categoryBox.setItems(FXCollections.observableArrayList(categories.findAll()));
        parentCategoryBox.setItems(FXCollections.observableArrayList(categories.findAll()));
        cartItemBox.setItems(FXCollections.observableArrayList(items.findByStatusOrderByNameAsc("ACTIVE")));
        stockItemBox.setItems(FXCollections.observableArrayList(items.findByStatusOrderByNameAsc("ACTIVE")));
    }
    private void loadAccounts() {
        accountTable.setItems(FXCollections.observableArrayList(accounts.findAllByOrderByUsernameAsc()));
        accountRoleBox.setItems(FXCollections.observableArrayList(roles.findAll()));
    }
    private void loadInventory() {
        stockTable.setItems(FXCollections.observableArrayList(stocks.findAll()));
        stockWarehouseBox.setItems(FXCollections.observableArrayList(warehouses.findAll()));
    }
    private void loadOrders() {
        if (orderTable != null && currentAccount != null) {
            orderTable.setItems(FXCollections.observableArrayList(orderService.ordersFor(currentAccount)));
            customerOrderTable.setItems(FXCollections.observableArrayList(orderService.ordersFor(currentAccount)));
        }
    }
    private void loadRevenue() {
        if (revenueChart == null) return;
        Map<Integer, BigDecimal> values = reports.revenueByMonth(LocalDate.now().getYear());
        todayRevenueLabel.setText("Doanh thu hôm nay: " + format(reports.revenueToday()) + " VND");
        revenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu " + LocalDate.now().getYear());
        for (int i = 1; i <= 12; i++) series.getData().add(new XYChart.Data<>("T" + i, values.getOrDefault(i, BigDecimal.ZERO)));
        revenueChart.getData().add(series);
    }

    @FXML void saveItem() {
        try {
            Item item = itemTable.getSelectionModel().getSelectedItem();
            if (item == null) item = new Item();
            item.setSku(skuField.getText().trim()); item.setName(nameField.getText().trim());
            item.setBasePrice(new BigDecimal(priceField.getText().trim())); item.setCategory(categoryBox.getValue());
            item.setStatus("ACTIVE"); catalog.saveItem(item); loadCatalog(); resultLabel.setText("Đã lưu sản phẩm.");
        } catch (Exception e) { showError(e); }
    }
    @FXML void saveCategory() {
        try {
            Category c = new Category(); c.setCategoryName(categoryNameField.getText().trim());
            c.setParent(parentCategoryBox.getValue()); catalog.saveCategory(c);
            loadCatalog(); categoryNameField.clear(); resultLabel.setText("Đã lưu danh mục.");
        } catch (Exception e) { showError(e); }
    }
    @FXML void deleteItem() {
        try {
            Item item = itemTable.getSelectionModel().getSelectedItem();
            if (item == null) throw new IllegalArgumentException("Hãy chọn sản phẩm.");
            catalog.deleteItem(item.getItemId()); loadCatalog(); resultLabel.setText("Đã xóa sản phẩm.");
        } catch (Exception e) { showError(e); }
    }
    @FXML void importItems() {
        FileChooser chooser = new FileChooser(); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showOpenDialog(itemTable.getScene().getWindow());
        try { if (file != null) { int n = catalog.importItems(Path.of(file.toURI()), categoryBox.getValue()); loadCatalog(); resultLabel.setText("Đã nhập " + n + " dòng."); } }
        catch (Exception e) { showError(e); }
    }
    @FXML void exportItems() {
        FileChooser chooser = new FileChooser(); chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(itemTable.getScene().getWindow()); if (file == null) return;
        try (FileWriter w = new FileWriter(file)) {
            w.write("SKU,Name,BasePrice,Status\n");
            for (Item i : itemTable.getItems()) w.write(String.format("\"%s\",\"%s\",%s,\"%s\"%n", i.getSku(), i.getName(), i.getBasePrice(), i.getStatus()));
        } catch (Exception e) { showError(e); }
    }
    @FXML void saveAccount() {
        try { accountService.save(currentAccount, accountUsernameField.getText(), accountPasswordField.getText(), accountRoleBox.getValue().getRoleName()); loadAccounts(); resultLabel.setText("Đã lưu tài khoản."); }
        catch (Exception e) { showError(e); }
    }
    @FXML void toggleAccount() {
        try { accountService.toggleStatus(currentAccount, accountTable.getSelectionModel().getSelectedItem().getAccountId()); loadAccounts(); }
        catch (Exception e) { showError(e); }
    }
    @FXML void saveWarehouse() {
        try {
            Warehouse w = new Warehouse(); w.setCode(warehouseCodeField.getText()); w.setName(warehouseNameField.getText());
            inventory.saveWarehouse(currentAccount, w); loadInventory(); resultLabel.setText("Đã lưu kho.");
        }
        catch (Exception e) { showError(e); }
    }
    @FXML void saveStock() {
        try {
            StockItem s = stockTable.getSelectionModel().getSelectedItem();
            if (s == null) s = new StockItem();
            s.setItem(stockItemBox.getValue()); s.setWarehouse(stockWarehouseBox.getValue());
            s.setQuantityOnHand(Integer.parseInt(stockOnHandField.getText())); s.setQuantityReserved(Integer.parseInt(stockReservedField.getText()));
            inventory.saveStock(currentAccount, s); loadInventory(); resultLabel.setText("Đã lưu tồn kho."); }
        catch (Exception e) { showError(e); }
    }
    @FXML void placeOrder() {
        try {
            Warehouse w = warehouses.findAll().stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Chưa có kho."));
            orderService.placeOrder(currentAccount, cartItemBox.getValue().getItemId(), w.getWarehouseId(),
                    Integer.parseInt(cartQuantityField.getText()), promotionField.getText(), paymentMethodBox.getValue());
            loadAll(); resultLabel.setText("Đã tạo đơn hàng.");
        } catch (Exception e) { showError(e); }
    }
    @FXML void transitionOrder() {
        try { orderService.transition(currentAccount, orderTable.getSelectionModel().getSelectedItem().getOrderId(), statusBox.getValue()); loadOrders(); }
        catch (Exception e) { showError(e); }
    }
    @FXML void confirmPayment() {
        try { paymentService.confirm(currentAccount, orderTable.getSelectionModel().getSelectedItem().getOrderId()); loadOrders(); }
        catch (Exception e) { showError(e); }
    }
    @FXML void refund() {
        try { paymentService.refund(currentAccount, orderTable.getSelectionModel().getSelectedItem().getOrderId(), new BigDecimal(refundAmountField.getText())); loadOrders(); }
        catch (Exception e) { showError(e); }
    }
    @FXML void savePromotion() {
        try {
            Promotion p = new Promotion();
            p.setCode(promotionCodeField.getText().trim());
            p.setDiscountType(promotionTypeBox.getValue());
            p.setValue(new BigDecimal(promotionValueField.getText().trim()));
            promotionService.save(currentAccount, p);
            resultLabel.setText("Đã lưu khuyến mãi.");
        } catch (Exception e) { showError(e); }
    }
    @FXML void changePassword() {
        try {
            authService.changePassword(currentAccount, oldPasswordField.getText(), newPasswordField.getText());
            resultLabel.setText("Đã đổi mật khẩu.");
            oldPasswordField.clear(); newPasswordField.clear();
        } catch (Exception e) { showError(e); }
    }
    @FXML void refresh() { loadAll(); }
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
            showError(ex);
        }
    }

    private String format(BigDecimal v) { return String.format("%,.0f", v == null ? BigDecimal.ZERO : v); }
    private void showError(Exception e) {
        String message = e.getMessage();
        Throwable cause = e;
        while ((message == null || message.isBlank()) && cause.getCause() != null) {
            cause = cause.getCause(); message = cause.getMessage();
        }
        if (message == null || message.isBlank()) message = "Thao tác thất bại.";
        if (message.contains("constraint") || message.contains("ConstraintViolation")) {
            message = "Dữ liệu bị trùng hoặc đang được sử dụng. Hãy kiểm tra mã/SKU và bản ghi liên quan.";
        }
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}

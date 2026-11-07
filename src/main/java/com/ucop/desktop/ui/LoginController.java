package com.ucop.desktop.ui;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {
    private final AuthService authService;
    private final ApplicationContext context;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    public LoginController(AuthService authService, ApplicationContext context) {
        this.authService = authService;
        this.context = context;
    }

    @FXML
    void login() {
        try {
            Account account = authService.authenticate(usernameField.getText().trim(), passwordField.getText());
            boolean customer = account.getRole() != null
                    && "CUSTOMER".equalsIgnoreCase(account.getRole().getRoleName());
            String view = customer ? "/fxml/customer-shop.fxml" : "/fxml/dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
            loader.setControllerFactory(context::getBean);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 1100, 700));
            if (customer) {
                ((CustomerController) loader.getController()).setCurrentAccount(account);
                stage.setTitle("UCOP - Cửa hàng công nghệ");
            } else {
                ((DashboardController) loader.getController()).setCurrentAccount(account);
                stage.setTitle("UCOP - Quản trị vận hành");
            }
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    void openRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            loader.setControllerFactory(context::getBean);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 600, 520));
            stage.setTitle("UCOP - Đăng ký khách hàng");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }
}

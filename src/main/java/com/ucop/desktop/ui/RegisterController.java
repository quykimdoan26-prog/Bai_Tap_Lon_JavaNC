package com.ucop.desktop.ui;

import com.ucop.desktop.service.RegistrationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {
    private final RegistrationService registrationService;
    private final ApplicationContext context;
    @FXML private TextField usernameField, fullNameField, emailField, phoneField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Label messageLabel;

    public RegisterController(RegistrationService registrationService, ApplicationContext context) {
        this.registrationService = registrationService;
        this.context = context;
    }

    @FXML
    void register() {
        try {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
            }
            registrationService.registerCustomer(usernameField.getText(), passwordField.getText(),
                    fullNameField.getText(), emailField.getText(), phoneField.getText());
            messageLabel.setText("Đăng ký thành công. Bạn có thể đăng nhập ngay.");
            usernameField.clear(); passwordField.clear(); confirmPasswordField.clear();
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    void backToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(context::getBean);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 520, 360));
            stage.setTitle("UCOP - Đăng nhập");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }
}

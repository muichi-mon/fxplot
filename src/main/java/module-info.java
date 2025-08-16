module io.github.rajveer.fxplot {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.rajveer.fxplot to javafx.fxml;
    exports io.github.rajveer.fxplot;
}
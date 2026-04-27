package com.example.matt_petschauer_cs360_sensor_assignment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Project Three app activity with login, SQLite CRUD, and optional SMS alerts.
 */
public class MainActivity extends AppCompatActivity {

    private static final String ALERT_PHONE_NUMBER = "5554"; // Emulator test number.
    private static final int LOW_INVENTORY_THRESHOLD = 2;

    private AppDatabaseHelper databaseHelper;

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText itemNameInput;
    private EditText quantityInput;

    private LinearLayout loginContainer;
    private LinearLayout appContainer;
    private TextView loginStatusText;
    private TextView smsPermissionStatus;
    private GridView inventoryGrid;

    private ArrayAdapter<String> gridAdapter;
    private final ArrayList<String> gridItems = new ArrayList<>();

    private boolean smsPermissionGranted;

    private final ActivityResultLauncher<String> smsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                smsPermissionGranted = isGranted;
                updateSmsPermissionStatus();
                if (isGranted) {
                    Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "SMS permission denied. App still works without SMS.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new AppDatabaseHelper(this);
        initializeViews();
        setupGrid();

        smsPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        updateSmsPermissionStatus();
    }

    private void initializeViews() {
        loginContainer = findViewById(R.id.loginContainer);
        appContainer = findViewById(R.id.appContainer);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        itemNameInput = findViewById(R.id.itemNameInput);
        quantityInput = findViewById(R.id.quantityInput);

        loginStatusText = findViewById(R.id.loginStatusText);
        smsPermissionStatus = findViewById(R.id.smsPermissionStatus);
        inventoryGrid = findViewById(R.id.inventoryGrid);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button requestSmsButton = findViewById(R.id.requestSmsButton);
        Button addButton = findViewById(R.id.addItemButton);
        Button updateButton = findViewById(R.id.updateItemButton);
        Button deleteButton = findViewById(R.id.deleteItemButton);
        Button refreshButton = findViewById(R.id.refreshGridButton);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
        requestSmsButton.setOnClickListener(v -> requestSmsPermission());
        addButton.setOnClickListener(v -> createInventoryItem());
        updateButton.setOnClickListener(v -> updateInventoryItem());
        deleteButton.setOnClickListener(v -> deleteInventoryItem());
        refreshButton.setOnClickListener(v -> refreshInventoryGrid());
    }

    private void setupGrid() {
        gridAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gridItems);
        inventoryGrid.setAdapter(gridAdapter);
    }

    private void loginUser() {
        String username = readTrimmedText(usernameInput);
        String password = readTrimmedText(passwordInput);

        if (!validateCredentials(username, password)) {
            return;
        }

        if (databaseHelper.isValidLogin(username, password)) {
            loginStatusText.setText("Login successful. Welcome, " + username + "!");
            loginContainer.setVisibility(View.GONE);
            appContainer.setVisibility(View.VISIBLE);
            refreshInventoryGrid();
        } else {
            loginStatusText.setText("Invalid username/password. Register first if new user.");
        }
    }

    private void registerUser() {
        String username = readTrimmedText(usernameInput);
        String password = readTrimmedText(passwordInput);

        if (!validateCredentials(username, password)) {
            return;
        }

        if (databaseHelper.registerUser(username, password)) {
            loginStatusText.setText("Registration successful. You can now log in.");
        } else {
            loginStatusText.setText("Registration failed. Username may already exist.");
        }
    }

    private boolean validateCredentials(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            loginStatusText.setText("Please enter both username and password.");
            return false;
        }
        return true;
    }

    private void createInventoryItem() {
        String itemName = readTrimmedText(itemNameInput);
        Integer quantity = parseQuantityInput();

        if (TextUtils.isEmpty(itemName) || quantity == null) {
            return;
        }

        if (databaseHelper.addItem(itemName, quantity)) {
            Toast.makeText(this, "Item added.", Toast.LENGTH_SHORT).show();
            maybeSendLowInventoryAlert(itemName, quantity);
        } else {
            Toast.makeText(this, "Add failed. Item may already exist.", Toast.LENGTH_SHORT).show();
        }
        refreshInventoryGrid();
    }

    private void updateInventoryItem() {
        String itemName = readTrimmedText(itemNameInput);
        Integer quantity = parseQuantityInput();

        if (TextUtils.isEmpty(itemName) || quantity == null) {
            return;
        }

        if (databaseHelper.updateItem(itemName, quantity)) {
            Toast.makeText(this, "Item updated.", Toast.LENGTH_SHORT).show();
            maybeSendLowInventoryAlert(itemName, quantity);
        } else {
            Toast.makeText(this, "Update failed. Item not found.", Toast.LENGTH_SHORT).show();
        }
        refreshInventoryGrid();
    }

    private void deleteInventoryItem() {
        String itemName = readTrimmedText(itemNameInput);

        if (TextUtils.isEmpty(itemName)) {
            Toast.makeText(this, "Enter an item name to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.deleteItem(itemName)) {
            Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Delete failed. Item not found.", Toast.LENGTH_SHORT).show();
        }
        refreshInventoryGrid();
    }

    private void refreshInventoryGrid() {
        gridItems.clear();
        Cursor cursor = databaseHelper.getAllItems();

        try {
            while (cursor.moveToNext()) {
                @SuppressWarnings("Range") String itemName = cursor.getString(cursor.getColumnIndex(AppDatabaseHelper.COL_ITEM_NAME));
                @SuppressWarnings("Range") int quantity = cursor.getInt(cursor.getColumnIndex(AppDatabaseHelper.COL_QUANTITY));
                gridItems.add(itemName + "\nQty: " + quantity);
            }
        } finally {
            cursor.close();
        }

        if (gridItems.isEmpty()) {
            gridItems.add("No inventory items yet.");
        }

        gridAdapter.notifyDataSetChanged();
    }

    private Integer parseQuantityInput() {
        String quantityText = readTrimmedText(quantityInput);
        if (TextUtils.isEmpty(quantityText)) {
            Toast.makeText(this, "Enter a quantity.", Toast.LENGTH_SHORT).show();
            return null;
        }

        try {
            return Integer.parseInt(quantityText);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Quantity must be a valid number.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            smsPermissionGranted = true;
            updateSmsPermissionStatus();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsPermissionGranted = true;
            updateSmsPermissionStatus();
            return;
        }

        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }

    private void maybeSendLowInventoryAlert(String itemName, int quantity) {
        if (quantity > LOW_INVENTORY_THRESHOLD) {
            return;
        }

        if (!smsPermissionGranted) {
            Toast.makeText(this, "Low inventory detected, but SMS permission is denied.", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = "Low inventory alert: " + itemName + " is at quantity " + quantity + ".";
        try {
            SmsManager.getDefault().sendTextMessage(ALERT_PHONE_NUMBER, null, message, null, null);
            Toast.makeText(this, "Low inventory SMS alert sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, "SMS failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSmsPermissionStatus() {
        smsPermissionStatus.setText(smsPermissionGranted
                ? "SMS permission: Granted"
                : "SMS permission: Denied (core app functions still available)");
    }

    private String readTrimmedText(@NonNull EditText input) {
        return input.getText().toString().trim();
    }
}

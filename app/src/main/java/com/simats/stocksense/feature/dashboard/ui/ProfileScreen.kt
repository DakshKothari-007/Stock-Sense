package com.simats.stocksense.feature.dashboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.theme.StockSenseTheme
import com.simats.stocksense.data.model.StoreProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    storeProfile: StoreProfile = StoreProfile(),
    isProfileLoading: Boolean = false,
    profileErrorMessage: String? = null,
    onRetryClick: () -> Unit = {},
    onUpdateShop: (String, String, String, (Boolean, String?) -> Unit) -> Unit = { _, _, _, cb -> cb(true, null) },
    onShopNameChange: (String) -> Unit = {},
    onUserNameChange: (String) -> Unit = {},
    onEmailChange: (String) -> Unit = {},
    onUpdateProfile: (String, String, (Boolean, String?) -> Unit) -> Unit = { _, _, cb -> cb(true, null) },
    onChangePassword: (String, String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _, cb -> cb(true, "Password changed successfully") },
    onSettingsClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    var profileSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Edit Shop Dialog State
    var showEditShopDialog by remember { mutableStateOf(false) }
    var shopNameInput by remember(storeProfile.name) { mutableStateOf(storeProfile.name) }
    var locationInput by remember(storeProfile.location) { mutableStateOf(storeProfile.location) }
    var storeTypeInput by remember(storeProfile.storeType) { mutableStateOf(storeProfile.storeType) }
    var storeTypeExpanded by remember { mutableStateOf(false) }
    var isSavingShop by remember { mutableStateOf(false) }
    var shopDialogErrorMessage by remember { mutableStateOf<String?>(null) }

    val storeTypeOptions = remember {
        listOf(
            "Neighbourhood supermarket",
            "Medium supermarket",
            "Grocery / Kirana store",
            "Other"
        )
    }

    // Edit Profile Info Dialog State
    var showEditUserInfoDialog by remember { mutableStateOf(false) }
    var userNameInput by remember(storeProfile.ownerName) { mutableStateOf(storeProfile.ownerName) }
    var emailInput by remember(storeProfile.email) { mutableStateOf(storeProfile.email) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var profileDialogErrorMessage by remember { mutableStateOf<String?>(null) }

    var showChangePasswordDialog by remember { mutableStateOf(false) }

    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isChangingPassword by remember { mutableStateOf(false) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }

    val initials = remember(storeProfile.ownerName) {
        storeProfile.ownerName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
    }

    val isCurrentPasswordValid = currentPasswordInput.isNotEmpty()
    val isNewPasswordValid = newPasswordInput.length >= 8
    val isConfirmPasswordValid = newPasswordInput == confirmPasswordInput
    val isChangePasswordEnabled = isCurrentPasswordValid && isNewPasswordValid && isConfirmPasswordValid
    val showMismatchError = confirmPasswordInput.isNotEmpty() && !isConfirmPasswordValid

    LaunchedEffect(storeProfile.name, storeProfile.location, storeProfile.storeType) {
        shopNameInput = storeProfile.name
        locationInput = storeProfile.location
        storeTypeInput = storeProfile.storeType
    }

    LaunchedEffect(storeProfile.ownerName, storeProfile.email) {
        userNameInput = storeProfile.ownerName
        emailInput = storeProfile.email
    }

    // Edit Shop Details Dialog
    if (showEditShopDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSavingShop) {
                    showEditShopDialog = false
                    shopDialogErrorMessage = null
                }
            },
            title = {
                Text(
                    text = "Edit Shop Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Update your shop workspace, location, and category:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = shopNameInput,
                        onValueChange = {
                            if (it.length <= 150) shopNameInput = it
                            shopDialogErrorMessage = null
                        },
                        label = { Text("Shop name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    OutlinedTextField(
                        value = locationInput,
                        onValueChange = {
                            if (it.length <= 255) locationInput = it
                            shopDialogErrorMessage = null
                        },
                        label = { Text("Location") },
                        placeholder = { Text("e.g. Indiranagar, Bengaluru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = storeTypeExpanded,
                        onExpandedChange = { storeTypeExpanded = !storeTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = storeTypeInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Store type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = storeTypeExpanded,
                            onDismissRequest = { storeTypeExpanded = false }
                        ) {
                            storeTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        storeTypeInput = option
                                        storeTypeExpanded = false
                                        shopDialogErrorMessage = null
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = storeProfile.currency.ifBlank { "₹ INR / IST" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency (Read-only)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false
                    )

                    if (!shopDialogErrorMessage.isNullOrBlank()) {
                        Text(
                            text = shopDialogErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSavingShop = true
                        shopDialogErrorMessage = null
                        onUpdateShop(
                            shopNameInput.trim(),
                            locationInput.trim(),
                            storeTypeInput.trim()
                        ) { success, msg ->
                            isSavingShop = false
                            if (success) {
                                showEditShopDialog = false
                                profileSuccessMessage = msg ?: "Shop details updated successfully."
                            } else {
                                shopDialogErrorMessage = msg ?: "Failed to update shop details."
                            }
                        }
                    },
                    enabled = shopNameInput.isNotBlank() && !isSavingShop,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (isSavingShop) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isSavingShop) {
                            showEditShopDialog = false
                            shopDialogErrorMessage = null
                        }
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Edit User Profile Info Dialog (Username & Email)
    if (showEditUserInfoDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSavingProfile) {
                    showEditUserInfoDialog = false
                    profileDialogErrorMessage = null
                }
            },
            title = {
                Text(
                    text = "Edit Profile Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Update your name and email address:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = {
                            userNameInput = it
                            profileDialogErrorMessage = null
                        },
                        label = { Text("Full Name / Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            profileDialogErrorMessage = null
                        },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    if (!profileDialogErrorMessage.isNullOrBlank()) {
                        Text(
                            text = profileDialogErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSavingProfile = true
                        profileDialogErrorMessage = null
                        onUpdateProfile(userNameInput.trim(), emailInput.trim()) { success, msg ->
                            isSavingProfile = false
                            if (success) {
                                showEditUserInfoDialog = false
                                profileSuccessMessage = msg ?: "Profile details updated successfully."
                            } else {
                                profileDialogErrorMessage = msg ?: "Failed to update profile."
                            }
                        }
                    },
                    enabled = userNameInput.isNotBlank() && emailInput.contains("@") && emailInput.contains(".") && !isSavingProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (isSavingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isSavingProfile) {
                            showEditUserInfoDialog = false
                            profileDialogErrorMessage = null
                        }
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                currentPasswordInput = ""
                newPasswordInput = ""
                confirmPasswordInput = ""
            },
            title = {
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Update your password to keep your account secure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    // Current Password
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = { currentPasswordInput = it },
                        label = { Text("Current password", fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val eyePath = Path().apply {
                                        moveTo(0f, h * 0.5f)
                                        quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
                                        quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
                                    }
                                    drawPath(
                                        path = eyePath,
                                        color = iconTint,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = iconTint,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!currentPasswordVisible) {
                                        drawLine(
                                            color = iconTint,
                                            start = Offset(w * 0.15f, h * 0.15f),
                                            end = Offset(w * 0.85f, h * 0.85f),
                                            strokeWidth = 1.8.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // New Password
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("New password (min 8 chars)", fontSize = 12.sp) },
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val eyePath = Path().apply {
                                        moveTo(0f, h * 0.5f)
                                        quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
                                        quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
                                    }
                                    drawPath(
                                        path = eyePath,
                                        color = iconTint,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = iconTint,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!newPasswordVisible) {
                                        drawLine(
                                            color = iconTint,
                                            start = Offset(w * 0.15f, h * 0.15f),
                                            end = Offset(w * 0.85f, h * 0.85f),
                                            strokeWidth = 1.8.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Confirm New Password
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm new password", fontSize = 12.sp) },
                        singleLine = true,
                        isError = showMismatchError,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Canvas(modifier = Modifier.size(18.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val eyePath = Path().apply {
                                        moveTo(0f, h * 0.5f)
                                        quadraticTo(w * 0.5f, 0f, w, h * 0.5f)
                                        quadraticTo(w * 0.5f, h, 0f, h * 0.5f)
                                    }
                                    drawPath(
                                        path = eyePath,
                                        color = iconTint,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = iconTint,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!confirmPasswordVisible) {
                                        drawLine(
                                            color = iconTint,
                                            start = Offset(w * 0.15f, h * 0.15f),
                                            end = Offset(w * 0.85f, h * 0.85f),
                                            strokeWidth = 1.8.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    )

                    if (showMismatchError) {
                        Text(
                            text = "Passwords do not match",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }

                    if (!passwordErrorMessage.isNullOrBlank()) {
                        Text(
                            text = passwordErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isChangingPassword = true
                        passwordErrorMessage = null
                        onChangePassword(
                            currentPasswordInput,
                            newPasswordInput,
                            confirmPasswordInput
                        ) { success, msg ->
                            isChangingPassword = false
                            if (success) {
                                passwordSuccessMessage = msg
                                showChangePasswordDialog = false
                                currentPasswordInput = ""
                                newPasswordInput = ""
                                confirmPasswordInput = ""
                            } else {
                                passwordErrorMessage = msg
                            }
                        }
                    },
                    enabled = isChangePasswordEnabled && !isChangingPassword,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (isChangingPassword) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Update Password")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                        passwordErrorMessage = null
                        isChangingPassword = false
                        currentPasswordInput = ""
                        newPasswordInput = ""
                        confirmPasswordInput = ""
                    }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Header
        Column(modifier = Modifier.fillMaxWidth()) {
            val displayTitle = when {
                storeProfile.name.isNotBlank() -> storeProfile.name
                storeProfile.ownerName.isNotBlank() -> storeProfile.ownerName
                else -> "Profile"
            }
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "STOCKSENSE WORKSPACE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp,
                fontSize = 10.sp
            )
        }

        if (isProfileLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = primaryColor
            )
        }

        if (!profileErrorMessage.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Failed to load profile",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profileErrorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (profileSuccessMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.1f, size.height * 0.55f)
                                lineTo(size.width * 0.35f, size.height * 0.8f)
                                lineTo(size.width * 0.75f, size.height * 0.25f)
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = profileSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { profileSuccessMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (passwordSuccessMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.1f, size.height * 0.55f)
                                lineTo(size.width * 0.35f, size.height * 0.8f)
                                lineTo(size.width * 0.75f, size.height * 0.25f)
                            }
                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = passwordSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { passwordSuccessMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. User Info Card with Edit Option
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row: Avatar + Name + Subtitle + Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = storeProfile.ownerName.ifBlank { "Store Owner" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val roleTitle = storeProfile.role.ifBlank { "Owner" }
                            val roleSubtitle = if (storeProfile.name.isNotBlank()) {
                                "$roleTitle · ${storeProfile.name}"
                            } else {
                                roleTitle
                            }
                            Text(
                                text = roleSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            userNameInput = storeProfile.ownerName
                            emailInput = storeProfile.email
                            profileDialogErrorMessage = null
                            showEditUserInfoDialog = true
                        }
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 13.sp
                        )
                    }
                }

                if (storeProfile.email.isNotBlank()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)
                    InfoRow("Email address", storeProfile.email)
                }
            }
        }

        // 3. Store Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.15f, size.height * 0.4f)
                                    lineTo(size.width * 0.5f, size.height * 0.15f)
                                    lineTo(size.width * 0.85f, size.height * 0.4f)
                                    lineTo(size.width * 0.85f, size.height * 0.85f)
                                    lineTo(size.width * 0.15f, size.height * 0.85f)
                                    close()
                                }
                                drawPath(path = path, color = primaryColor, style = Stroke(width = 1.8.dp.toPx()))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = storeProfile.name.ifBlank { "Store details" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                            if (storeProfile.location.isNotBlank()) {
                                Text(
                                    text = storeProfile.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            shopNameInput = storeProfile.name
                            locationInput = storeProfile.location
                            storeTypeInput = storeProfile.storeType
                            shopDialogErrorMessage = null
                            showEditShopDialog = true
                        }
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 12.sp
                        )
                    }
                }

                val hasDetails = storeProfile.storeType.isNotBlank() ||
                        storeProfile.storeId.isNotBlank() ||
                        storeProfile.currency.isNotBlank() ||
                        storeProfile.lastUpdated.isNotBlank()

                if (hasDetails) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                    if (storeProfile.storeType.isNotBlank()) {
                        InfoRow("Store type", storeProfile.storeType)
                    }
                    if (storeProfile.storeId.isNotBlank()) {
                        InfoRow("Store ID", storeProfile.storeId)
                    }
                    if (storeProfile.currency.isNotBlank()) {
                        InfoRow("Currency / timezone", storeProfile.currency)
                    }
                    if (storeProfile.lastUpdated.isNotBlank()) {
                        InfoRow("Data snapshot", storeProfile.lastUpdated)
                    }
                }
            }
        }

        // 4. Plan Info Banner
        if (storeProfile.plan.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val path = Path().apply {
                                moveTo(cx, 0f)
                                quadraticTo(cx, cy, size.width, cy)
                                quadraticTo(cx, cy, cx, size.height)
                                quadraticTo(cx, cy, 0f, cy)
                                quadraticTo(cx, cy, cx, 0f)
                            }
                            drawPath(path = path, color = primaryColor, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = storeProfile.plan,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sample plan · Up to 500 products",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI recommendations & sales intelligence included",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 5. Change Password Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { showChangePasswordDialog = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawArc(
                                color = primaryColor,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(size.width * 0.28f, size.height * 0.1f),
                                size = Size(size.width * 0.44f, size.height * 0.45f),
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(size.width * 0.18f, size.height * 0.42f),
                                size = Size(size.width * 0.64f, size.height * 0.48f),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = size.width * 0.08f,
                                center = Offset(size.width * 0.5f, size.height * 0.64f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Change Password",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Security & login credentials",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        // 6. Settings Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSettingsClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawCircle(color = primaryColor, radius = size.width * 0.3f, style = Stroke(width = 1.8.dp.toPx()))
                            drawCircle(color = primaryColor, radius = size.width * 0.45f, style = Stroke(width = 1.2.dp.toPx()))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                }

                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        // 7. Sign Out Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSignOutClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(18.dp)) {
                            drawRect(
                                color = primaryColor,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.2f),
                                size = Size(size.width * 0.7f, size.height * 0.7f),
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                }

                Canvas(modifier = Modifier.size(10.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, size.height * 0.1f)
                        lineTo(size.width * 0.8f, size.height * 0.5f)
                        lineTo(size.width * 0.2f, size.height * 0.9f)
                    }
                    drawPath(path = path, color = primaryColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    StockSenseTheme {
        ProfileContent()
    }
}

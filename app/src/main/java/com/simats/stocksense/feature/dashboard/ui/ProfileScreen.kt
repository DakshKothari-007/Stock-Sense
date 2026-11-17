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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.stocksense.core.components.*
import com.simats.stocksense.core.theme.*
import com.simats.stocksense.core.util.ResponsiveContainer
import com.simats.stocksense.core.util.rememberWindowSizeInfo
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
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
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
            modifier = Modifier
                .widthIn(max = 560.dp)
                .border(2.5.dp, InkDark, RoundedCornerShape(16.dp)),
            title = {
                Text(
                    text = "Edit Shop Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
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
                        color = MutedInk
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
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
                                focusedBorderColor = InkDark,
                                unfocusedBorderColor = InkDark,
                                focusedContainerColor = CardWhite,
                                unfocusedContainerColor = CardWhite,
                                focusedTextColor = InkDark,
                                unfocusedTextColor = InkDark
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = storeTypeExpanded,
                            onDismissRequest = { storeTypeExpanded = false }
                        ) {
                            storeTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = InkDark) },
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
                            focusedBorderColor = InkDark.copy(alpha = 0.5f),
                            unfocusedBorderColor = InkDark.copy(alpha = 0.3f),
                            disabledBorderColor = InkDark.copy(alpha = 0.3f),
                            disabledTextColor = MutedInk,
                            disabledLabelColor = MutedInk
                        ),
                        enabled = false
                    )

                    if (!shopDialogErrorMessage.isNullOrBlank()) {
                        Text(
                            text = shopDialogErrorMessage ?: "",
                            color = StatusDangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                BoldButton(
                    text = "Save",
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
                    isLoading = isSavingShop,
                    isPrimary = true
                )
            },
            dismissButton = {
                BoldButton(
                    text = "Cancel",
                    onClick = {
                        if (!isSavingShop) {
                            showEditShopDialog = false
                            shopDialogErrorMessage = null
                        }
                    },
                    isPrimary = false
                )
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = CardWhite
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
            modifier = Modifier
                .widthIn(max = 560.dp)
                .border(2.5.dp, InkDark, RoundedCornerShape(16.dp)),
            title = {
                Text(
                    text = "Edit Profile Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Update your name and email address:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
                        )
                    )

                    if (!profileDialogErrorMessage.isNullOrBlank()) {
                        Text(
                            text = profileDialogErrorMessage ?: "",
                            color = StatusDangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                BoldButton(
                    text = "Save",
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
                    isLoading = isSavingProfile,
                    isPrimary = true
                )
            },
            dismissButton = {
                BoldButton(
                    text = "Cancel",
                    onClick = {
                        if (!isSavingProfile) {
                            showEditUserInfoDialog = false
                            profileDialogErrorMessage = null
                        }
                    },
                    isPrimary = false
                )
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = CardWhite
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
            modifier = Modifier
                .widthIn(max = 560.dp)
                .border(2.5.dp, InkDark, RoundedCornerShape(16.dp)),
            title = {
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
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
                        color = MutedInk,
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
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
                                        color = InkDark,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = InkDark,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!currentPasswordVisible) {
                                        drawLine(
                                            color = InkDark,
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark
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
                                        color = InkDark,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = InkDark,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!newPasswordVisible) {
                                        drawLine(
                                            color = InkDark,
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
                            focusedBorderColor = InkDark,
                            unfocusedBorderColor = InkDark,
                            focusedContainerColor = CardWhite,
                            unfocusedContainerColor = CardWhite,
                            focusedTextColor = InkDark,
                            unfocusedTextColor = InkDark,
                            errorBorderColor = StatusDangerRed
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
                                        color = InkDark,
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = InkDark,
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.5f, h * 0.5f)
                                    )
                                    if (!confirmPasswordVisible) {
                                        drawLine(
                                            color = InkDark,
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
                            color = StatusDangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }

                    if (!passwordErrorMessage.isNullOrBlank()) {
                        Text(
                            text = passwordErrorMessage ?: "",
                            color = StatusDangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                BoldButton(
                    text = "Update Password",
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
                    isLoading = isChangingPassword,
                    isPrimary = true
                )
            },
            dismissButton = {
                BoldButton(
                    text = "Cancel",
                    onClick = {
                        showChangePasswordDialog = false
                        passwordErrorMessage = null
                        isChangingPassword = false
                        currentPasswordInput = ""
                        newPasswordInput = ""
                        confirmPasswordInput = ""
                    },
                    isPrimary = false
                )
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = CardWhite
        )
    }

    val windowInfo = rememberWindowSizeInfo()

    ResponsiveContainer(
        modifier = Modifier.fillMaxSize(),
        maxWidth = 720.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.horizontalPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                    color = InkDark,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "STOCKSENSE WORKSPACE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MainBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                )
            }

            if (isProfileLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MainBlue,
                    trackColor = PaleBlue
                )
            }

            if (!profileErrorMessage.isNullOrBlank()) {
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SoftPink,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Failed to load profile",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusDangerRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profileErrorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkDark,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        BoldButton(
                            text = "Retry",
                            onClick = onRetryClick,
                            isPrimary = true,
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            }

            if (profileSuccessMessage != null) {
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PaleBlue,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    contentPadding = PaddingValues(14.dp)
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
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.1f, size.height * 0.55f)
                                    lineTo(size.width * 0.35f, size.height * 0.8f)
                                    lineTo(size.width * 0.75f, size.height * 0.25f)
                                }
                                drawPath(
                                    path = path,
                                    color = MainBlue,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = profileSuccessMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
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
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (passwordSuccessMessage != null) {
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PaleBlue,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 2.dp,
                    contentPadding = PaddingValues(14.dp)
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
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width * 0.1f, size.height * 0.55f)
                                    lineTo(size.width * 0.35f, size.height * 0.8f)
                                    lineTo(size.width * 0.75f, size.height * 0.25f)
                                }
                                drawPath(
                                    path = path,
                                    color = MainBlue,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = passwordSuccessMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
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
                                color = InkDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. User Info Card with Edit Option
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header row: Avatar + Name + Subtitle + Edit Button
                    val isNarrow = windowInfo.screenWidthDp < 360.dp
                    if (isNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MainYellow)
                                        .border(2.dp, InkDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = InkDark,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = storeProfile.ownerName.ifBlank { "Store Owner" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 17.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                        color = MutedInk,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            BoldButton(
                                text = "Edit",
                                onClick = {
                                    userNameInput = storeProfile.ownerName
                                    emailInput = storeProfile.email
                                    profileDialogErrorMessage = null
                                    showEditUserInfoDialog = true
                                },
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
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
                                        .background(MainYellow)
                                        .border(2.dp, InkDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = InkDark,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = storeProfile.ownerName.ifBlank { "Store Owner" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 17.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                        color = MutedInk,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            BoldButton(
                                text = "Edit",
                                onClick = {
                                    userNameInput = storeProfile.ownerName
                                    emailInput = storeProfile.email
                                    profileDialogErrorMessage = null
                                    showEditUserInfoDialog = true
                                },
                                isPrimary = false,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }

                    if (storeProfile.email.isNotBlank()) {
                        HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)
                        InfoRow("Email address", storeProfile.email)
                    }
                }
            }

            // 3. Store Info Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isNarrow = windowInfo.screenWidthDp < 360.dp
                    if (isNarrow) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SoftLavender)
                                        .border(1.5.dp, InkDark, RoundedCornerShape(12.dp)),
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
                                        drawPath(path = path, color = InkDark, style = Stroke(width = 1.8.dp.toPx()))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = storeProfile.name.ifBlank { "Store details" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (storeProfile.location.isNotBlank()) {
                                        Text(
                                            text = storeProfile.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            BoldButton(
                                text = "Edit",
                                onClick = {
                                    shopNameInput = storeProfile.name
                                    locationInput = storeProfile.location
                                    storeTypeInput = storeProfile.storeType
                                    shopDialogErrorMessage = null
                                    showEditShopDialog = true
                                },
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
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
                                        .background(SoftLavender)
                                        .border(1.5.dp, InkDark, RoundedCornerShape(12.dp)),
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
                                        drawPath(path = path, color = InkDark, style = Stroke(width = 1.8.dp.toPx()))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = storeProfile.name.ifBlank { "Store details" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (storeProfile.location.isNotBlank()) {
                                        Text(
                                            text = storeProfile.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedInk,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            BoldButton(
                                text = "Edit",
                                onClick = {
                                    shopNameInput = storeProfile.name
                                    locationInput = storeProfile.location
                                    storeTypeInput = storeProfile.storeType
                                    shopDialogErrorMessage = null
                                    showEditShopDialog = true
                                },
                                isPrimary = false,
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }

                    val hasDetails = storeProfile.storeType.isNotBlank() ||
                            storeProfile.storeId.isNotBlank() ||
                            storeProfile.currency.isNotBlank() ||
                            storeProfile.lastUpdated.isNotBlank()

                    if (hasDetails) {
                        HorizontalDivider(color = InkDark.copy(alpha = 0.15f), thickness = 1.dp)

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
                BoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PaleBlue,
                    borderColor = InkDark,
                    borderWidth = 2.dp,
                    shadowOffset = 3.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardWhite)
                                .border(1.5.dp, InkDark, CircleShape),
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
                                drawPath(path = path, color = MainBlue, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = storeProfile.plan,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkDark,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sample plan · Up to 500 products",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AI recommendations & sales intelligence included",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MainBlue,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 5. Change Password Option Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                onClick = { showChangePasswordDialog = true },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftLightBlue)
                                .border(1.5.dp, InkDark, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawArc(
                                    color = InkDark,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(size.width * 0.28f, size.height * 0.1f),
                                    size = Size(size.width * 0.44f, size.height * 0.45f),
                                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawRoundRect(
                                    color = InkDark,
                                    topLeft = Offset(size.width * 0.18f, size.height * 0.42f),
                                    size = Size(size.width * 0.64f, size.height * 0.48f),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                                drawCircle(
                                    color = InkDark,
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
                                color = InkDark,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Security & login credentials",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedInk,
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
                        drawPath(path = path, color = InkDark, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            // 6. Settings Option Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                onClick = { onSettingsClick() },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftPeach)
                                .border(1.5.dp, InkDark, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawCircle(color = InkDark, radius = size.width * 0.3f, style = Stroke(width = 1.8.dp.toPx()))
                                drawCircle(color = InkDark, radius = size.width * 0.45f, style = Stroke(width = 1.2.dp.toPx()))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkDark,
                            fontSize = 15.sp
                        )
                    }

                    Canvas(modifier = Modifier.size(10.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.1f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.9f)
                        }
                        drawPath(path = path, color = InkDark, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            // 7. Sign Out Option Card
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = CardWhite,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                onClick = { onSignOutClick() },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftLavender)
                                .border(1.5.dp, InkDark, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                drawRect(
                                    color = InkDark,
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
                            color = InkDark,
                            fontSize = 15.sp
                        )
                    }

                    Canvas(modifier = Modifier.size(10.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.1f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.9f)
                        }
                        drawPath(path = path, color = InkDark, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            // 8. Delete Account Option Card (Warning/Destructive Action)
            BoldCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SoftPink,
                borderColor = InkDark,
                borderWidth = 2.dp,
                shadowOffset = 3.dp,
                onClick = { onDeleteAccountClick() },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardWhite)
                                .border(1.5.dp, InkDark, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(18.dp)) {
                                val w = size.width
                                val h = size.height
                                // Trash can body
                                val body = Path().apply {
                                    moveTo(w * 0.25f, h * 0.35f)
                                    lineTo(w * 0.3f, h * 0.85f)
                                    lineTo(w * 0.7f, h * 0.85f)
                                    lineTo(w * 0.75f, h * 0.35f)
                                    close()
                                }
                                drawPath(
                                    path = body,
                                    color = StatusDangerRed,
                                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                // Lid
                                drawLine(
                                    color = StatusDangerRed,
                                    start = Offset(w * 0.15f, h * 0.35f),
                                    end = Offset(w * 0.85f, h * 0.35f),
                                    strokeWidth = 1.6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Handle
                                drawArc(
                                    color = StatusDangerRed,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(w * 0.38f, h * 0.2f),
                                    size = Size(w * 0.24f, h * 0.24f),
                                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Delete account",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusDangerRed,
                            fontSize = 15.sp
                        )
                    }

                    Canvas(modifier = Modifier.size(10.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.1f)
                            lineTo(size.width * 0.8f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.9f)
                        }
                        drawPath(
                            path = path,
                            color = StatusDangerRed,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
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
            color = MutedInk,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            fontSize = 12.sp,
            maxLines = 1
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

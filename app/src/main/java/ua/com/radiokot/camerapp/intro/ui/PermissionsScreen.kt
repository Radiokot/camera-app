package ua.com.radiokot.camerapp.intro.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ua.com.radiokot.camerapp.ui.LeTextButton

@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    permissions: ImmutableList<String>,
    onAllPermissionsGranted: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsResult ->
            if (permissionsResult.all(Map.Entry<String, Boolean>::value)) {
                onAllPermissionsGranted()
            } else {
                // TODO explain.
            }
        },
    )

    Box(
        modifier = modifier
            .safeContentPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        LeTextButton(
            text = "Grant permissions",
            onClick = {
                permissionLauncher.launch(
                    permissions.toTypedArray(),
                )
            },
        )
    }
}

@Preview
@Composable
private fun PermissionsScreenPreview(

) {
    PermissionsScreen(
        permissions =
            persistentListOf(
                Manifest.permission.CAMERA,
            ),
        onAllPermissionsGranted = {},
        modifier = Modifier
            .fillMaxSize()
    )
}

package ua.com.radiokot.camerapp.intro.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.koin.compose.viewmodel.koinViewModel

const val PermissionsDestination = "permissions"

fun NavGraphBuilder.permissionsDestination(
    onDone: () -> Unit,
) = composable(
    route = PermissionsDestination,
) {
    val viewModel: PermissionsScreenViewModel = koinViewModel()

    PermissionsScreen(
        permissions = viewModel.requiredPermissions,
        onAllPermissionsGranted = viewModel::onAllPermissionsGranted,
        modifier = Modifier
            .fillMaxSize()
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PermissionsScreenViewModel.Event.Done -> {
                    onDone()
                }
            }
        }
    }
}

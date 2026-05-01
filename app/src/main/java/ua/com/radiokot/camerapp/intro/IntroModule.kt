package ua.com.radiokot.camerapp.intro

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.com.radiokot.camerapp.intro.ui.PermissionsScreenViewModel

val introModule = module {

    viewModel {
        PermissionsScreenViewModel(

        )
    }
}

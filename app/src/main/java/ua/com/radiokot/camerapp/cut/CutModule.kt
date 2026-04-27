package ua.com.radiokot.camerapp.cut

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.com.radiokot.camerapp.cut.ui.ImageAdjustmentsControllerViewModel
import ua.com.radiokot.camerapp.cut.ui.StampCutScreenViewModel
import ua.com.radiokot.camerapp.cut.ui.StampSaveScreenViewModel

val cutModule = module {

    viewModel {
        StampCutScreenViewModel()
    }

    viewModel {
        ImageAdjustmentsControllerViewModel()
    }

    viewModel {
        StampSaveScreenViewModel(
            stampRepository = get(),
            imageAdjustmentsControllerViewModel = get(),
            parameters =
                getOrNull()
                    ?: error("No parameters provided for StampSaveScreenViewModel"),
        )
    }
}

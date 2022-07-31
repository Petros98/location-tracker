package task.gg.locationtracker.ui.activity

sealed interface UiState {
    object Default : UiState

    object Started : UiState

    object Stopped : UiState

    object ReadyForTracking : UiState

    enum class RequireAction : UiState { REQUEST_PERMISSIONS, ENABLE_LOCATION }
}

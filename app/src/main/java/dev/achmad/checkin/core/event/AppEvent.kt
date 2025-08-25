package dev.achmad.checkin.core.event

import dev.achmad.checkin.core.di.util.injectLazy
import dev.achmad.checkin.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun onSignOut() {
        _events.tryEmit(AppEvent.SignOut)
        scope.launch {
            val repository by injectLazy<AuthRepository>()
            repository.signOut()
        }
    }
}

sealed interface AppEvent {
    data object SignOut: AppEvent
}
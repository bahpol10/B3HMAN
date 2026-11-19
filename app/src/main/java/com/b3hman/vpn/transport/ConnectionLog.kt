package com.b3hman.vpn.transport

import com.b3hman.vpn.core.DiagnosticsLog

internal object ConnectionLog {
    fun record(message: String) = DiagnosticsLog.i("Transport", message)
}

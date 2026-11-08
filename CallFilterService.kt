package com.dollcii.vipringer

import android.telecom.Call
import android.telecom.CallScreeningService

class CallFilterService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val builder = CallResponse.Builder()
        if (details.callDirection == Call.Details.DIRECTION_INCOMING && Store.isEnabled(this)) {
            val number = details.handle?.schemeSpecificPart ?: ""
            if (!Store.isAllowed(this, number)) {
                builder.setSilenceCall(true) // يسكّت الرنة بس، والمكالمة تفضل ظاهرة وتتسجل
            }
        }
        respondToCall(details, builder.build())
    }
}

package com.example.studentapp

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.google.gson.Gson

class MyHostApduService : HostApduService() {

    companion object {
        var studentId: String = ""
        var studentName: String = ""

        private val AID = byteArrayOf(
            0xF0.toByte(), 0x43, 0x4C, 0x41, 0x53, 0x53, 0x01
        )
        val SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val FAILURE = byteArrayOf(0x6A.toByte(), 0x82.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        return if (isSelectAidCommand(commandApdu)) {
            if (studentId.isEmpty()) {
                FAILURE
            } else {
                val payload = buildPayload()
                payload.toByteArray(Charsets.UTF_8) + SUCCESS
            }
        } else {
            FAILURE
        }
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < 5 + AID.size) return false
        // Провери дали е SELECT AID (00 A4 04 00)
        if (apdu[0] != 0x00.toByte()) return false
        if (apdu[1] != 0xA4.toByte()) return false
        if (apdu[2] != 0x04.toByte()) return false
        return true
    }

    private fun buildPayload(): String {
        return Gson().toJson(mapOf(
            "studentId" to studentId,
            "studentName" to studentName,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    override fun onDeactivated(reason: Int) { }
}
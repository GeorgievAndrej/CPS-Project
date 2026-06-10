package com.example.studentapp

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
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
        val hex = commandApdu.joinToString(" ") { "%02X".format(it) }
        Log.d("HCE_DEBUG", "Примен APDU: $hex")

        return if (isSelectAidCommand(commandApdu)) {
            if (studentId.isEmpty()) {
                Log.w("HCE_DEBUG", "studentId е празен!")
                FAILURE
            } else {
                val payload = buildPayload()
                Log.d("HCE_DEBUG", "Испраќам: $payload")
                payload.toByteArray(Charsets.UTF_8) + SUCCESS
            }
        } else {
            Log.w("HCE_DEBUG", "Не е SELECT AID команда")
            FAILURE
        }
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        // Минимум: 5 header bytes + 7 AID bytes = 12
        if (apdu.size < 5 + AID.size) return false
        if (apdu[0] != 0x00.toByte()) return false
        if (apdu[1] != 0xA4.toByte()) return false
        if (apdu[2] != 0x04.toByte()) return false
        // ПОПРАВЕНО: Провери ги AID bytes еден по еден
        // Без ова одговара на СЕКОЈА SELECT команда
        for (i in AID.indices) {
            if (apdu[5 + i] != AID[i]) return false
        }
        return true
    }

    private fun buildPayload(): String {
        return Gson().toJson(mapOf(
            "studentId"   to studentId,
            "studentName" to studentName,
            "timestamp"   to System.currentTimeMillis()
        ))
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE_DEBUG", "Деактивиран")
    }
}
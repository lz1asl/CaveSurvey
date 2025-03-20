package com.astoev.cave.survey.test.service.bluetooth.device

import com.astoev.cave.survey.exception.DataException
import com.astoev.cave.survey.service.bluetooth.device.AbstractBluetoothDevice
import com.astoev.cave.survey.service.bluetooth.device.ble.bosch.BoschGLM50_27CDevice
import com.astoev.cave.survey.service.bluetooth.device.protocol.BoschGlmDeviceProtocol
import com.bosch.mtprotocol.MtMessage
import com.bosch.mtprotocol.glm100C.message.edc.EDCInputMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BoschGlmProtocolTest : AbstractDeviceProtocolTest() {

    @Test
    fun testDistanceMessage() {
        ensureSucces(getMessage(0.2658f), 0.266f, null, null)
        ensureSucces(getMessage(1.6427499f), 1.643f, null, null)
    }

    fun getMessage(distance: Float): MtMessage {
        val message = EDCInputMessage()
        message.devMode = EDCInputMessage.MODE_SINGLE_DISTANCE
        message.result = distance
        return message
    }

    protected fun ensureSucces(
        aMessage: MtMessage,
        aDistance: Float?,
        anAzimuth: Float?,
        anAngle: Float?
    ) {
        try {
            val measures = BoschGlmDeviceProtocol.decodeMessage(aMessage, deviceSpec)
            assertMeasurements(aDistance, anAzimuth, anAngle, measures)
        } catch (de: DataException) {
            Assertions.fail<Any>("Message not recognized: " + de.message)
        }
    }

    override fun getDeviceSpec(): AbstractBluetoothDevice {
        return BoschGLM50_27CDevice()
    }


}
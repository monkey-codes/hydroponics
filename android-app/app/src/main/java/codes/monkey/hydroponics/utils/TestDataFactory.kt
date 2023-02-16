package codes.monkey.hydroponics.utils

import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.network.Device
import java.time.Instant

object TestDataFactory {

    fun createCamera(
        id: String = "video0",
        deviceId: String = "b8:27:eb:66:03:0b",
        created: Long = Instant.now().toEpochMilli(),
        pk: String = "devices#b8:27:eb:66:03:0b",
        sk: String = "cameras#video0"

    ) = Camera(id = id, deviceId = deviceId, created = created, pk = pk, sk = sk)

    fun createDevice(
        id: String = "b8:27:eb:66:03:0b",
        created: Long = Instant.now().toEpochMilli(),
        pk: String = "devices",
        sk: String = "devices#b8:27:eb:66:03:0b"

    ) = Device(id = id, created = created, pk = pk, sk = sk)
}
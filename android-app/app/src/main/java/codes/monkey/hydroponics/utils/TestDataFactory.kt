package codes.monkey.hydroponics.utils

import codes.monkey.hydroponics.network.Camera
import java.time.Instant

object TestDataFactory {

    fun createCamera(
        id: String = "video0",
        deviceId: String = "b8:27:eb:66:03:0b",
        created: Long = Instant.now().toEpochMilli(),
        pk: String = "devices#b8:27:eb:66:03:0b",
        sk: String = "cameras#video0"

    ) = Camera(id = id, deviceId = deviceId, created = created, pk = pk, sk = sk)

}
package com.meisapps.pcbiounlock.server.pairing

import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.server.BasicServer
import com.meisapps.pcbiounlock.server.ConnectedClient
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.PairedDevice
import com.meisapps.pcbiounlock.service.api.PCBUApi
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.AESUtils
import com.meisapps.pcbiounlock.utils.exceptions.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.text.StringUtils
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PacketPairInit(var protoVersion: String, var deviceName: String, var ipAddress: String, var cloudToken: String)

@Serializable
data class PacketPairResponse(var errMsg: String, var pairingId: String, var pairingMethod: PairingMethod, var hostName: String, var hostOS: String, var macAddress: String, var userName: String, var password: String)

@Serializable
data class PairingQRData(val ip: String, val port: Int, val method: Int, val encKey: String)

data class UserData(val userName: String, val password: String)

class PairingServer(private val deviceStorage: DeviceStorage, private val pairingMethod: PairingMethod, private val userData: UserData)
    : BasicServer("0.0.0.0", AppSettings.get().pairingServerPort, StringUtils.generateRandomString(32)) {
    var bluetoothAddress = ""

    private var devicePairedListener: ((PacketPairResponse) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null

    fun getQRText() : String {
        val settings = AppSettings.get()
        var ipStr = settings.serverIP
        if(ipStr == "auto")
            ipStr = PCBUApi.getLocalIP()
        val data = PairingQRData(ipStr, settings.pairingServerPort, pairingMethod.ordinal, encryptionKey)
        return Json.encodeToString(PairingQRData.serializer(), data)
    }

    fun setOnDevicePairedListener(u: (PacketPairResponse) -> Unit) {
        devicePairedListener = u
    }

    fun setOnErrorListener(u: (String) -> Unit) {
        errorListener = u
    }

    fun runBlocking() {
        setOnDevicePairedListener {
            Console.println("Pairing finished.")
            stop()
        }

        start()
        while (isRunning)
            Thread.sleep(50)
        stop()
    }

    override fun onDataReceived(client: ConnectedClient, data: ByteArray) {
        try {
            val decData = AESUtils.decryptPacketData(data, encryptionKey)
            val packet = Json.decodeFromString(PacketPairInit.serializer(), decData.toString(Charsets.UTF_8))
            if(packet.protoVersion != VersionInfo.getProtocolVersion())
                throw ErrorMessageException(I18n.get("error_app_version_mismatch"))

            val pairingId = AESUtils.sha256(NativeUtils.getForPlatform().getDeviceUUID() + packet.deviceName + userData.userName)
            val device = PairedDevice(pairingId, pairingMethod, packet.deviceName, userData.userName, encryptionKey, packet.ipAddress, bluetoothAddress, packet.cloudToken)
            deviceStorage.addDevice(device)

            val resultPacket = PacketPairResponse("",
                pairingId, pairingMethod, HostUtils.getDeviceName(), OperatingSystem.getString(), PCBUApi.getMacAddress(),
                userData.userName, userData.password)
            client.sendPacket(Json.encodeToString(PacketPairResponse.serializer(), resultPacket))
            devicePairedListener?.invoke(resultPacket)
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
            val resultPacket = PacketPairResponse(e.message!!, "", PairingMethod.TCP, "", "", "", "", "")
            client.sendPacket(Json.encodeToString(PacketPairResponse.serializer(), resultPacket))
            errorListener?.invoke(e.message!!)
        }
    }
}

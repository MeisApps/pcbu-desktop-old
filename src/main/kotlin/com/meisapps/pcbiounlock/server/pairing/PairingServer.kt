package com.meisapps.pcbiounlock.server.pairing

import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.server.BasicServer
import com.meisapps.pcbiounlock.server.ConnectedClient
import com.meisapps.pcbiounlock.server.packets.DataReaderStream
import com.meisapps.pcbiounlock.server.packets.DataWriterStream
import com.meisapps.pcbiounlock.server.packets.Packet
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.api.PCBUApi
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.meisapps.pcbiounlock.utils.text.StringUtils
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


class PacketPairDevice(var version: String, var deviceName: String, var messagingToken: String, var ipAddress: String) : Packet() {
    companion object {
        const val PacketId = 0
    }

    override fun getPacketId(): Int {
        return PacketId
    }

    override fun read(b: ByteArray) {
        val stream = DataReaderStream(b)
        version = stream.readString()
        deviceName = stream.readString()
        messagingToken = stream.readString()
        ipAddress = stream.readString()
    }
}

class PacketPairResult(var isPaired: Boolean, var message: String,
                       var pairingId: String, var name: String, var hostOS: String, var pairingMethod: PairingMethod,
                       var userName: String, var password: String) : Packet() {
    companion object {
        const val PacketId = 1
    }

    override fun getPacketId(): Int {
        return PacketId
    }

    override fun write(): ByteArray {
        val stream = DataWriterStream()
        stream.writeBoolean(isPaired)
        stream.writeString(message)
        stream.writeString(pairingId)
        stream.writeString(name)
        stream.writeString(hostOS)
        stream.writeInt(pairingMethod.ordinal)
        stream.writeString(userName)
        stream.writeString(password)
        return stream.toByteArray()
    }
}

@Serializable
data class PairingQRData(val ip: String, val port: Int, val method: Int, val encKey: String)

data class UserData(val userName: String, val password: String)
class PairingServer(private val deviceStorage: DeviceStorage, private val pairingMethod: PairingMethod, private val userData: UserData)
    : BasicServer("0.0.0.0", AppSettings.get().pairingServerPort, StringUtils.generateRandomString(32)) {
    var bluetoothAddress = ""

    private var devicePairedListener: ((PacketPairResult) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null

    fun getQRText() : String {
        val data = PairingQRData(PCBUApi.getLocalIP(), AppSettings.get().pairingServerPort, pairingMethod.ordinal, encryptionKey)
        return Json.encodeToString(PairingQRData.serializer(), data)
    }

    fun setOnDevicePairedListener(u: (PacketPairResult) -> Unit) {
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

    override fun onPacketReceived(client: ConnectedClient, packetId: Int, packetData: ByteArray) {
        when(packetId) {
            PacketPairDevice.PacketId -> {
                val packet = PacketPairDevice("", "", "", "")
                packet.read(packetData)

                pairDevice(client, packet)
            }
        }
    }

    private fun pairDevice(client: ConnectedClient, packet: PacketPairDevice) {
        if(pairingMethod == PairingMethod.TCP)
            bluetoothAddress = ""

        try {
            if(packet.version != VersionInfo.getProtocolVersion())
                throw ErrorMessageException(I18n.get("error_app_version_mismatch"))

            val pairingId = NativeUtils.getDeviceUUID()
            val hostName = HostUtils.getDeviceName()
            deviceStorage.savePairData(pairingId, packet.deviceName, packet.messagingToken, packet.ipAddress, bluetoothAddress, encryptionKey)

            val resultPacket = PacketPairResult(true, "",
                pairingId, hostName, OperatingSystem.getString(), pairingMethod,
                userData.userName, userData.password)
            client.sendPacket(resultPacket)

            devicePairedListener?.invoke(resultPacket)
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
            val resultPacket = PacketPairResult(false, e.message!!, "", "", "", PairingMethod.TCP, "", "")
            client.sendPacket(resultPacket)

            errorListener?.invoke(e.message!!)
        }
    }
}
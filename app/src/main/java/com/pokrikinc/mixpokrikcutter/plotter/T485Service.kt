package com.pokrikinc.mixpokrikcutter.plotter

import android.serialport.SerialPort
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Semaphore

class T485Service {
    private val tag = "T_485_Service"
    private var serialPort: SerialPort? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var readThread: ReadThread? = null

    var driverOpen = false
    var readData: String? = null
    var isPrint = false

    private var received = false
    private var buffer: ByteArray? = null
    private var size = -1
    private var xbxMsg = ""

    private val semaphore = Semaphore(1)

    fun start(devicePath: String, baudRate: String): Boolean {
        return try {
            val port = getSerialPort(devicePath, baudRate)
            this.serialPort = port
            this.outputStream = port.outputStream
            this.inputStream = port.inputStream

            this.readThread = ReadThread().apply { start() }
            this.driverOpen = true

            LogUtils.d(tag, "Последовательное соединение успешно")
            true
        } catch (e: IOException) {
            this.driverOpen = false
            LogUtils.e(tag, "Ошибка соединения последовательного порта ${e.message}")
            false
        }
    }

    fun close() {
        readThread?.let {
            try {
                it.setStop(true)
                readThread = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        outputStream?.let {
            try {
                it.close()
                outputStream = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        inputStream?.let {
            try {
                it.close()
                inputStream = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        serialPort?.let {
            try {
                it.close()
                serialPort = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        driverOpen = false
        LogUtils.e(tag, "Последовательный порт закрыт")
    }

    @Synchronized
    fun sendData(data: ByteArray): Boolean {
        return outputStream?.let { output ->
            try {
                output.write(data)
                output.flush()
                LogUtils.e(
                    tag,
                    "Последовательный порт успешно отправлен: ${
                        StringUtil.toHexString(
                            data,
                            data.size
                        )
                    }"
                )
                true
            } catch (e: IOException) {
                e.printStackTrace()
                LogUtils.e(tag, "Исключение отправки через последовательный порт")
                false
            }
        } ?: run {
            LogUtils.d(tag, "Ошибка инициализации последовательного порта")
            false
        }
    }

    private fun getSerialPort(devicePath: String, baudRate: String): SerialPort {
        return serialPort ?: SerialPort(File(devicePath), baudRate.toInt()).also {
            serialPort = it
        }
    }

    private fun reset() {
        xbxMsg = ""
        readData = null
        received = false
        buffer = null
        size = -1
    }

    fun callBack(data: ByteArray): Received {
        return try {
            reset()
            semaphore.acquire()

            if (sendData(data)) {
                var timeout = 5000

                while (!received && timeout > 0) {
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    timeout -= 50
                }

                if (received && buffer != null && size > 0) {
                    val responseBuffer = ByteArray(size)
                    System.arraycopy(buffer!!, 0, responseBuffer, 0, size)
                    Received(1, received, size, responseBuffer, xbxMsg).also {
                        semaphore.release()
                    }
                } else {
                    Received(2, false, 0, null, null).also {
                        semaphore.release()
                    }
                }
            } else {
                Received(3, false, 0, null, null).also {
                    semaphore.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Received(0, false, 0, null, null).also {
                semaphore.release()
            }
        }
    }

    fun notCallBack(data: ByteArray): Received {
        return try {
            reset()
            semaphore.acquire()

            if (sendData(data)) {
                Received(1, false, 0, null, null).also {
                    semaphore.release()
                }
            } else {
                Received(3, false, 0, null, null).also {
                    semaphore.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Received(0, false, 0, null, null).also {
                semaphore.release()
            }
        }
    }

    inner class ReadThread : Thread() {
        private var stop = false

        fun setStop(stop: Boolean) {
            this.stop = stop
        }

        override fun run() {
            super.run()

            while (!isInterrupted && !stop && inputStream != null) {
                try {
                    val tempBuffer = ByteArray(1024)
                    val bytesRead = inputStream!!.read(tempBuffer)

                    if (bytesRead > 0) {
                        buffer = tempBuffer
                        size = bytesRead
                        readData = StringUtil.toHexString(tempBuffer, bytesRead).replace(" ", "")
                        xbxMsg += readData!!

                        val hexStringToString = StringUtil.hexStringToString(xbxMsg)
                        val currentHexToString = StringUtil.hexStringToString(readData!!)

                        LogUtils.d(
                            tag,
                            "Получена информация о последовательном порте：$hexStringToString-----$currentHexToString"
                        )

                        if (isPrint) {
                            try {
                                if (hexStringToString.isNotEmpty() && hexStringToString.contains("RCMD=10,0")) {
                                    xbxMsg = ""
                                    readData = ""
                                    isPrint = false
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        if (hexStringToString.isNotEmpty() && hexStringToString.endsWith(";")) {
                            received = true
                        }
                    } else {
                        LogUtils.e(tag, "empty data")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

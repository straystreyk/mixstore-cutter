package com.example.testandroidapp.plotter

object PrintUtil {
    fun cmdDePassU32(value: Long): Long {
        return (((((value + 908153991) xor 1092948257) - 1361593975) xor 309809476) shl 32) ushr 32
    }

    fun getEdition(type: Int = 0): ByteArray {
        return when (type) {
            0 -> "RSVER;"
            1 -> "RHVER;"
            2 -> "RMODE;"
            else -> "RPGHEAD;"
        }.toByteArray()
    }

    fun getParameter(): ByteArray = "BD:101,9;".toByteArray()

    fun getState(): ByteArray = "BD:10;".toByteArray()

    fun getHandshake(value: Long): ByteArray {
        val processedValue =
            ((((value xor 89428503) + 36213271) xor 109659922) - 18096792) shl 32 ushr 32
        return "BD:12,$processedValue;".toByteArray()
    }

    fun getIP(): ByteArray = "BD:9;".toByteArray()

    fun getID(): ByteArray = ";;;RPID;".toByteArray()

    fun restart(): ByteArray = "BD:31,0;".toByteArray()

    fun shutdown(): ByteArray = "BD:31,1;".toByteArray()

    fun passU32(value: Int): ByteArray = "BD:9,8,$value;".toByteArray()

    fun setSpeedV(speed: Int): ByteArray = "BD:100,11,$speed;BD:101,9;".toByteArray()

    fun setSpeedF(speed: Int): ByteArray = "BD:100,10,$speed;BD:101,9;".toByteArray()

    fun setCutAlarm(alarm: String): ByteArray = "BD:108,$alarm;".toByteArray()

    fun getCountValue(): ByteArray = "BD:30,1;".toByteArray()

    fun initCountValue(): ByteArray = "BD:30,0;".toByteArray()

    fun writeID(): ByteArray = "BD:249,num,pass1;".toByteArray()

    fun readID(): ByteArray = "RCBM;".toByteArray()

    // Команды движения
    fun stop(): ByteArray = "BD:100,0;BD:10;".toByteArray()
    fun left(): ByteArray = "BD:100,1;BD:10;".toByteArray()
    fun right(): ByteArray = "BD:100,2;BD:10;".toByteArray()
    fun before(): ByteArray = "BD:100,3;BD:10;".toByteArray()
    fun after(): ByteArray = "BD:100,4;BD:10;".toByteArray()
    fun square(): ByteArray = "BD:100,8;BD:10;".toByteArray()

    // Тестовые команды
    fun test(): ByteArray = "BD:100,100;".toByteArray()
    fun test1(): ByteArray = "BD:100,101;".toByteArray()
    fun test2(): ByteArray = "BD:100,102;".toByteArray()
    fun test3(): ByteArray = "BD:100,103;".toByteArray()
    fun test4(): ByteArray = "BD:100,104;".toByteArray()
    fun test5(): ByteArray = "BD:100,105;".toByteArray()

    fun printFile(content: String): ByteArray = content.toByteArray()

    fun paperFeed(): ByteArray = "BD:33,-101;".toByteArray()

    fun queryAutoFeed(): ByteArray = "BD:34;".toByteArray()

    fun getMaxWidth(): ByteArray = "BD:100,20,0;".toByteArray()

    fun getPermission(): ByteArray = "BD:110,0;".toByteArray()
}
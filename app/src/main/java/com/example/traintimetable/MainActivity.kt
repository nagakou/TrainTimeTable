@file:Suppress("NAME_SHADOWING", "DEPRECATION")

package com.example.traintimetable

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.math.MathUtils
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.example.traintimetable.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.io.*
import java.lang.Thread.sleep
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.concurrent.timer


enum class Station(
    val staName: String,
    val staInfo: Int,
    val weekday: Int,
    val holiday: Int
){
    KO01("新宿", R.array.KO01, R.array.KO01w, R.array.KO01h),
    KO02("初台", R.array.KO02, R.array.KO02w, R.array.KO02h),
    KO03("幡ヶ谷", R.array.KO03, R.array.KO03w, R.array.KO03h),
    KO04("笹塚", R.array.KO04, R.array.KO04w, R.array.KO04h),
    KO05("代田橋", R.array.KO05, R.array.KO05w, R.array.KO05h),
    KO06("明大前", R.array.KO06, R.array.KO06w, R.array.KO06h),
    KO07("下高井戸", R.array.KO07, R.array.KO07w, R.array.KO07h),
    KO08("桜上水", R.array.KO08, R.array.KO08w, R.array.KO08h),
    KO09("上北沢", R.array.KO09, R.array.KO09w, R.array.KO09h),
    KO10("八幡山", R.array.KO10, R.array.KO10w, R.array.KO10h),
    KO11("芦花公園", R.array.KO11, R.array.KO11w, R.array.KO11h),
    KO12("千歳烏山", R.array.KO12, R.array.KO12w, R.array.KO12h),
    KO13("仙川", R.array.KO13, R.array.KO13w, R.array.KO13h),
    KO14("つつじヶ丘", R.array.KO14, R.array.KO14w, R.array.KO14h),
    KO15("柴崎", R.array.KO15, R.array.KO15w, R.array.KO15h),
    KO16("国領", R.array.KO16, R.array.KO16w, R.array.KO16h),
    KO17("布田", R.array.KO17, R.array.KO17w, R.array.KO17h),
    KO18("調布", R.array.KO18, R.array.KO18w, R.array.KO18h),
    KO19("西調布", R.array.KO19, R.array.KO19w, R.array.KO19h),
    KO20("飛田給", R.array.KO20, R.array.KO20w, R.array.KO20h),
    KO21("武蔵野台", R.array.KO21, R.array.KO21w, R.array.KO21h),
    KO22("多磨霊園", R.array.KO22, R.array.KO22w, R.array.KO22h),
    KO23("東府中", R.array.KO23, R.array.KO23w, R.array.KO23h),
    KO24("府中", R.array.KO24, R.array.KO24w, R.array.KO24h),
    KO25("分倍河原", R.array.KO25, R.array.KO25w, R.array.KO25h),
    KO26("中河原", R.array.KO26, R.array.KO26w, R.array.KO26h),
    KO27("聖蹟桜ヶ丘", R.array.KO27, R.array.KO27w, R.array.KO27h),
    KO28("百草園", R.array.KO28, R.array.KO28w, R.array.KO28h),
    KO29("高幡不動", R.array.KO29, R.array.KO29w, R.array.KO29h),
    KO30("南平", R.array.KO30, R.array.KO30w, R.array.KO30h),
    KO31("平山城址公園", R.array.KO31, R.array.KO31w, R.array.KO31h),
    KO32("長沼", R.array.KO32, R.array.KO32w, R.array.KO32h),
    KO33("北野", R.array.KO33, R.array.KO33w, R.array.KO33h),
    KO34("京王八王子", R.array.KO34, R.array.KO34w, R.array.KO34h),
    KO35("京王多摩川", R.array.KO35, R.array.KO35w, R.array.KO35h),
    KO36("京王稲田堤", R.array.KO36, R.array.KO36w, R.array.KO36h),
    KO37("京王よみうりランド", R.array.KO37, R.array.KO37w, R.array.KO37h),
    KO38("稲城", R.array.KO38, R.array.KO38w, R.array.KO38h),
    KO39("若葉台", R.array.KO39, R.array.KO39w, R.array.KO39h),
    KO40("京王永山", R.array.KO40, R.array.KO40w, R.array.KO40h),
    KO41("京王多摩センター", R.array.KO41, R.array.KO41w, R.array.KO41h),
    KO42("京王堀之内", R.array.KO42, R.array.KO42w, R.array.KO42h),
    KO43("南大沢", R.array.KO43, R.array.KO43w, R.array.KO43h),
    KO44("多摩境", R.array.KO44, R.array.KO44w, R.array.KO44h),
    KO45("橋本", R.array.KO45, R.array.KO45w, R.array.KO45h),
    KO46("府中競馬正門前", R.array.KO46, R.array.KO46w, R.array.KO46h),
    KO47("多摩動物公園", R.array.KO47, R.array.KO47w, R.array.KO47h),
    KO48("京王片倉", R.array.KO48, R.array.KO48w, R.array.KO48h),
    KO49("山田", R.array.KO49, R.array.KO49w, R.array.KO49h),
    KO50("めじろ台", R.array.KO50, R.array.KO50w, R.array.KO50h),
    KO51("狭間", R.array.KO51, R.array.KO51w, R.array.KO51h),
    KO52("高尾", R.array.KO52, R.array.KO52w, R.array.KO52h),
    KO53("高尾山口", R.array.KO53, R.array.KO53w, R.array.KO53h)

}
enum class StationIno(
    val staName: String,
    val staInfo: Int,
    val weekday: Int,
    val holiday: Int
){
    IN01("渋谷", R.array.IN01, R.array.IN01w, R.array.IN01h),
    IN02("神泉", R.array.IN02, R.array.IN02w, R.array.IN02h),
    IN03("駒場東大前", R.array.IN03, R.array.IN03w, R.array.IN03h),
    IN04("池ノ上", R.array.IN04, R.array.IN04w, R.array.IN04h),
    IN05("下北沢", R.array.IN05, R.array.IN05w, R.array.IN05h),
    IN06("新代田", R.array.IN06, R.array.IN06w, R.array.IN06h),
    IN07("東松原", R.array.IN07, R.array.IN07w, R.array.IN07h),
    IN08("明大前", R.array.IN08, R.array.IN08w, R.array.IN08h),
    IN09("永福町", R.array.IN09, R.array.IN09w, R.array.IN09h),
    IN10("西永福", R.array.IN10, R.array.IN10w, R.array.IN10h),
    IN11("浜田山", R.array.IN11, R.array.IN11w, R.array.IN11h),
    IN12("高井戸", R.array.IN12, R.array.IN12w, R.array.IN12h),
    IN13("富士見ヶ丘", R.array.IN13, R.array.IN13w, R.array.IN13h),
    IN14("久我山", R.array.IN14, R.array.IN14w, R.array.IN14h),
    IN15("三鷹台", R.array.IN15, R.array.IN15w, R.array.IN15h),
    IN16("井の頭公園", R.array.IN16, R.array.IN16w, R.array.IN16h),
    IN17("吉祥寺", R.array.IN17, R.array.IN17w, R.array.IN17h)
}
enum class StationToei(
    val staName: String,
    val staInfo: Int,
    val weekday: Int,
    val holiday: Int
){
    S01("新宿", R.array.S01, R.array.S01w, R.array.S01h),
    S02("新宿三丁目", R.array.S02, R.array.S02w, R.array.S02h),
    S03("曙橋", R.array.S03, R.array.S03w, R.array.S03h),
    S04("市ヶ谷", R.array.S04, R.array.S04w, R.array.S04h),
    S05("九段下", R.array.S05, R.array.S05w, R.array.S05h),
    S06("神保町", R.array.S06, R.array.S06w, R.array.S06h),
    S07("小川町", R.array.S07, R.array.S07w, R.array.S07h),
    S08("岩本町", R.array.S08, R.array.S08w, R.array.S08h),
    S09("馬喰横山", R.array.S09, R.array.S09w, R.array.S09h),
    S10("浜町", R.array.S10, R.array.S10w, R.array.S10h),
    S11("森下", R.array.S11, R.array.S11w, R.array.S11h),
    S12("菊川", R.array.S12, R.array.S12w, R.array.S12h),
    S13("住吉", R.array.S13, R.array.S13w, R.array.S13h),
    S14("西大島", R.array.S14, R.array.S14w, R.array.S14h),
    S15("大島", R.array.S15, R.array.S15w, R.array.S15h),
    S16("東大島", R.array.S16, R.array.S16w, R.array.S16h),
    S17("船堀", R.array.S17, R.array.S17w, R.array.S17h),
    S18("一之江", R.array.S18, R.array.S18w, R.array.S18h),
    S19("瑞江", R.array.S19, R.array.S19w, R.array.S19h),
    S20("篠崎", R.array.S20, R.array.S20w, R.array.S20h),
    S21("本八幡", R.array.S21, R.array.S21w, R.array.S21h)
}
class MainActivity : Activity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private var time: ArrayList<String> = ArrayList()
    private var type: ArrayList<Int> = ArrayList()
    private var bound: ArrayList<String> = ArrayList()
    private var n = 0
    private val staLat : ArrayList<Double> = ArrayList()
    private val staLon : ArrayList<Double> = ArrayList()
    private val staLatIno : ArrayList<Double> = ArrayList()
    private val staLonIno : ArrayList<Double> = ArrayList()
    private val staLatToei : ArrayList<Double> = ArrayList()
    private val staLonToei : ArrayList<Double> = ArrayList()
    private var page = 1
    private val prefFileName = "com.example.traintimetable.app.PREF_FILE_NAME"
    private var nowLat = 0.0
    private var nowLon = 0.0
    private var nearestStaNo = 0
    private var nearestStaDistance = "0m"
    private var isNearestSta = false
    private var isLocationPermission = false
    private lateinit var mDetector : GestureDetector
    private lateinit var gestureListener : View.OnTouchListener
    private var totalSwipe : Float = 0f
    private var startSwipe : Float = 0f
    private var changeSwipeR : Boolean = false
    private var changeSwipeL : Boolean = false
    private var settingNowPage : Int = 0
    private var settingNow : Boolean = false
    data class Lang(
        val position: Int,
        val sn: Array<String>,
        val stnView: TextView,
        val day: TextView,
        val listView: ListView,
        val changeButton: Button
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Lang

            if (position != other.position) return false
            if (!sn.contentEquals(other.sn)) return false
            if (stnView != other.stnView) return false
            if (day != other.day) return false
            if (listView != other.listView) return false
            if (changeButton != other.changeButton) return false

            return true
        }

        override fun hashCode(): Int {
            var result = position
            result = 31 * result + sn.contentHashCode()
            result = 31 * result + stnView.hashCode()
            result = 31 * result + day.hashCode()
            result = 31 * result + listView.hashCode()
            result = 31 * result + changeButton.hashCode()
            return result
        }
    }
    data class Lang2(
        val position: Int,
        val pos: Int,
        val sn2: Array<String>,
        val stn: String,
        val day: TextView,
        val listView: ListView,
        val changeButton: Button,
        val sn: Array<String>,
        val stnView: TextView
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Lang2

            if (position != other.position) return false
            if (pos != other.pos) return false
            if (!sn2.contentEquals(other.sn2)) return false
            if (stn != other.stn) return false
            if (day != other.day) return false
            if (listView != other.listView) return false
            if (changeButton != other.changeButton) return false
            if (!sn.contentEquals(other.sn)) return false
            if (stnView != other.stnView) return false

            return true
        }

        override fun hashCode(): Int {
            var result = position
            result = 31 * result + pos
            result = 31 * result + sn2.contentHashCode()
            result = 31 * result + stn.hashCode()
            result = 31 * result + day.hashCode()
            result = 31 * result + listView.hashCode()
            result = 31 * result + changeButton.hashCode()
            result = 31 * result + sn.contentHashCode()
            result = 31 * result + stnView.hashCode()
            return result
        }
    }
    private var lu2 : Lang? = null
    private var lu3 : Lang2? = null
    private var railway = 0 //1:京王線、2:井の頭線、3:都営新宿線
    private var nearestRailway = 0 //1:京王線、2:井の頭線、3:都営新宿線
    private var numberRing : ArrayList<Int> = ArrayList()
    private var numberRingIno : ArrayList<Int> = ArrayList()
    private var numberRingToei : ArrayList<Int> = ArrayList()
    private lateinit var locationCallback: LocationCallback
    private val DOWNLOAD_FILE_URL = "https://nagakou.official.jp/timetable/data.zip"
    private val DOWNLOAD_FILE_URL2 = "https://nagakou.official.jp/timetable/dataversion.csv"
    private lateinit var asyncfiledownload : AsyncFileDownload
    private lateinit var asyncfiledownload2 : AsyncFileDownload
    // 最大ファイル数
    private val unzipMaxEntries: Int = 1024 * 5
    // 単体の最大ファイルサイズ
    private val unzipMaxFileSize: Long = 1024L * 1024L * 1024L * 5L // 5GiB
    // 全ファイルの合計最大ファイルサイズ
    private val unzipMaxTotalSize: Long = 1024L * 1024L * 1024L * 5L // 5GiB
    // バッファサイズ (参考値: https://gihyo.jp/admin/clip/01/fdt/200810/31)
    private val unzipBufferSize: Int = 1024 * 1024 // 1MiB

    fun ZipInputStream.forEach(R: (ZipEntry)->Unit){
        var entry = nextEntry
        while (entry != null){
            R(entry)
            entry = nextEntry
        }
    }
    private fun extract(target: Path, filename: String? = target.toString()) {
        var `in`: ZipInputStream?
        var out: BufferedOutputStream?
        var zipEntry: ZipEntry?
        var len: Int
        try {
            `in` = ZipInputStream(FileInputStream(filename))

            // ZIPファイルに含まれるエントリに対して順にアクセス
            while (`in`.nextEntry.also { zipEntry = it } != null) {
                // 出力用ファイルストリームの生成
                val dst = File(target.parent.toFile(), zipEntry!!.name.replace('\\','/'))
                Log.d("unzip",dst.toString())
                if(zipEntry!!.name.indexOf(".csv")!=-1) {
                    dst.parentFile.mkdirs()
                    out = BufferedOutputStream(FileOutputStream(dst))
                    // エントリの内容を出力
                    val buffer = ByteArray(1024*1024)
                    while (`in`.read(buffer).also { len = it } != -1) {
                        out.write(buffer, 0, len)
                    }
                    `in`.closeEntry()
                    out.close()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.AppTheme)
        setContentView(binding.root)
        setContentView(R.layout.activity_main)
        val directory = File(this.filesDir.path+"/timetable")
        if (directory.exists() === false) {
            if (directory.mkdir() === true) {
            } else {
                val ts = Toast.makeText(this, "データ更新失敗", Toast.LENGTH_LONG)
                ts.show()
            }
        }
        val data = getSharedPreferences("Data", MODE_PRIVATE)
        val editor = data.edit()
        val outputFile2 = File(directory, "dataversion.csv")
        asyncfiledownload2 = AsyncFileDownload(this, DOWNLOAD_FILE_URL2, outputFile2)
        asyncfiledownload2.execute()
        while(asyncfiledownload2.loadedBytePercent!=100) sleep(1)
        val file = File(directory.path+"/dataversion.csv").readText()
        Log.d("dataversion(online)",file)
        if(data.getString("DataVersion", null)!=null){
            Log.d("dataversion(offline)", data.getString("DataVersion", null)!!.replace(" ",""))
            if(!data.getString("DataVersion", null)!!.replace(" ","").equals(file)){
                //Log.d("TAG","DIFFER VERSION")
                val outputFile = File(directory, "data.zip")
                asyncfiledownload = AsyncFileDownload(this, DOWNLOAD_FILE_URL, outputFile)
                asyncfiledownload.execute()
                while(asyncfiledownload.loadedBytePercent!=100) TimeUnit.SECONDS.sleep(1)
                //Log.d(TAG,asyncfiledownload.loadedBytePercent.toString())
                extract(Paths.get(this.filesDir.path+"/timetable/data.zip"))
                editor.putString("DataVersion", file)
            }
        }else{
            //Log.d("dataversion(error)", "Nothing data")
            val outputFile = File(directory, "data.zip")
            asyncfiledownload = AsyncFileDownload(this, DOWNLOAD_FILE_URL, outputFile)
            asyncfiledownload.execute()
            while(asyncfiledownload.loadedBytePercent!=100) TimeUnit.SECONDS.sleep(1)
            //Log.d(TAG,asyncfiledownload.loadedBytePercent.toString())
            extract(Paths.get(this.filesDir.path+"/timetable/data.zip"))
            editor.putString("DataVersion", file)
        }
        editor.apply();
        getLocation()
        numberRingSet()
        numberRingInoSet()
        numberRungTieiSet()
        var updatedCount = 0
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations){
                    updatedCount++
                    nowLat = location.latitude
                    nowLon = location.longitude
                    //Log.d("LocationUpdate","[${updatedCount}] ${location.latitude} , ${location.longitude}")
                }
            }
        }
    }
    private fun startLocationUpdates() {
        val locationRequest = createLocationRequest()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null)
    }
    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
    override fun onResume() {
        super.onResume()
        init()
        startLocationUpdates()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.repeatCount == 0) {
            when (keyCode) {
                KeyEvent.KEYCODE_STEM_1 -> {
                    // Do stuff
                    if(page!=1) {
                        page -= 1
                        pushButton()
                    }
                    true
                }
                KeyEvent.KEYCODE_STEM_2 -> {
                    // Do stuff
                    if(page!=5) {
                        page += 1
                        pushButton()
                    }
                    true
                }
                KeyEvent.KEYCODE_STEM_3 -> {
                    // Do stuff
                    true
                }
                else -> {
                    super.onKeyDown(keyCode, event)
                }
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
    private fun staLocationSet(){
        staLat.addAll(listOf(35.68982452605462, 35.6810283349311, 35.677348543933924, 35.673628192150886, 35.67111455953893, 35.66839709939523, 35.66619247863595, 35.66758568019751, 35.66884664797167, 35.66983038019688, 35.670603641673104, 35.668126139469145, 35.662312297026205, 35.65815536661608, 35.65415666248371, 35.65016163466615, 35.64988475294385, 35.651805416151085, 35.65709309759728, 35.660090197269426, 35.66419086357673, 35.66610939661082, 35.6689267963026, 35.672186839597956, 35.668419505609016, 35.659642231559324, 35.650824862120764, 35.65754403224696, 35.66216483073274, 35.654628697866805, 35.647598043337695, 35.64281299915911, 35.644460111655484, 35.65758266777211, 35.64444543355646, 35.63392518197642, 35.633090583468636, 35.63619330457267, 35.619412113691205, 35.63034082715949,35.62526107219954, 35.62455110338238, 35.614281748237325, 35.602078151555816, 35.59493337687607, 35.66861583541759, 35.6491519685861, 35.6445380836404, 35.64450230856552, 35.64342395881105, 35.64066931359887, 35.64196148414672, 35.63252816360701))
        staLon.addAll(listOf(139.69921946468264, 139.6861618936359, 139.67688368249517, 139.66702861029614, 139.6593440825418, 139.65049079788133, 139.6413751113765, 139.63190266904635, 139.62334201137656, 139.6161137402116, 139.60869049419244, 139.60115816611264, 139.5849082978812, 139.57535161322065, 139.56675009788103, 139.5583356690459, 139.55153641933813, 139.5447501113761, 139.53003678254134, 139.52345982487162, 139.51105044021122, 139.50296374021138, 139.49504778254172, 139.48018912671634, 139.46850396351311, 139.45787666904607, 139.44724685555073, 139.43079208254136, 139.4131858402113, 139.39196545555077, 139.38025866351254, 139.3660260402107, 139.35451825555054, 139.34380088152272, 139.53653696714093, 139.5312066697462, 139.51772746822036, 139.50032355440152, 139.47244788508624, 139.44822425439926, 139.42432626527255, 139.40032756075536, 139.38008868508433, 139.36708839300587, 139.34487945438568, 139.48485349303144, 139.40453424573994, 139.33719913905904, 139.32073505440462, 139.30766405440428, 139.2936409697488, 139.2825687446014, 139.26996488509133))
        staLatIno.addAll(listOf(35.65829855985537, 35.657298278774675, 35.65869795215304, 35.660501043908695, 35.66159604706526, 35.662602802870374, 35.66260805757326, 35.668449917986116, 35.676287433987774, 35.67862556761454, 35.68169523524594, 35.68326295857426, 35.68478698455163, 35.688156049178076, 35.69224349849842, 35.6974638269697, 35.7026612703611))
        staLonIno.addAll(listOf(139.6985560429942, 139.69363575631215, 139.6844477497436, 139.67303094876368, 139.66688462437153, 139.66048231379062, 139.65573293560712, 139.65049346479114, 139.64261848620296, 139.63543927991844, 139.62739141306716, 139.61519874737095, 139.60719634471636, 139.59930064623634, 139.5889094072647, 139.5826715437751, 139.58007780241164))
        staLatToei.addAll(listOf(35.68982452605462, 35.69073534694457, 35.69239551423711, 35.69152869236803, 35.69541727049955, 35.69593427373408, 35.69516524568743, 35.69558296022361, 35.691862309223914, 35.68843559968077, 35.68802140957655, 35.68839704645723, 35.68902048022927, 35.689348087446334, 35.689762665474476, 35.689860902342375, 35.683842685825674, 35.68598334708547, 35.6932828281498, 35.70598747231804, 35.722732339944564))
        staLonToei.addAll(listOf(139.69921946468264, 139.7064453041124, 139.72262611480653, 139.73757680467907, 139.75119438255257, 139.75762610080983, 139.76685478488787, 139.77508314207108, 139.78308093375702, 139.78792640213877, 139.79730395979684, 139.80610383055213, 139.8158459145332, 139.8263629028881, 139.83474039102884, 139.84751760815672, 139.86413351751435, 139.88308527299776, 139.8977290105528, 139.90368766515877, 139.92645417049047))
    }
    private fun allNearestStation(){
        //Log.d(TAG, "Location: $nowLat,$nowLon")
        var distance = 1000000000000.0f
        var distance2 = 1000000000000.0f
        var distance3 = 1000000000000.0f
        val results = FloatArray(3)
        var nearestStaKONo = 0
        var nearestStaKODis = 0f
        var nearestStaINNo = 0
        var nearestStaINDis = 0f
        var nearestStaSNo = 0
        var nearestStaSDis = 0f
        //Log.d(TAG, "nearestStation: "+staLon.size)
        for(i in staLon.indices){
            Location.distanceBetween(nowLat, nowLon, staLat[i], staLon[i], results)
            //Log.d(TAG, "nearestStation: $i : " + results[0])
            if(distance > results[0]){
                distance = results[0]
                nearestStaKODis = distance
                nearestStaKONo = i
            }
        }
        //Log.d(TAG, "nearestStation: "+staLonIno.size)
        for(i in staLonIno.indices){
            Location.distanceBetween(nowLat, nowLon, staLatIno[i], staLonIno[i], results)
            //Log.d(TAG, "nearestStation: $i : " + results[0])
            if(distance2 > results[0]){
                distance2 = results[0]
                nearestStaINDis = distance2
                nearestStaINNo = i
            }
        }
        //Log.d(TAG, "nearestStation: "+staLonToei.size)
        for(i in staLonToei.indices){
            Location.distanceBetween(nowLat, nowLon, staLatToei[i], staLonToei[i], results)
            //Log.d(TAG, "nearestStation: $i : " + results[0])
            if(distance3 > results[0]){
                distance3 = results[0]
                nearestStaSDis = distance3
                nearestStaSNo = i
            }
        }
        //Log.d(TAG,"$nearestStaKODis or $nearestStaINDis")
        if(nearestStaKODis < nearestStaINDis && nearestStaKODis < nearestStaSDis){
            nearestStaDistance = if(nearestStaKODis < 1000.toDouble()) nearestStaKODis.toInt().toString() + "m"
            else "%,.1f".format(nearestStaKODis/1000) + "km"
            nearestStaNo = nearestStaKONo
            nearestRailway = 1
        }else if(nearestStaINDis < nearestStaKODis && nearestStaINDis < nearestStaSDis){
            nearestStaDistance = if(nearestStaINDis < 1000.toDouble()) nearestStaINDis.toInt().toString() + "m"
            else "%,.1f".format(nearestStaINDis/1000) + "km"
            nearestStaNo = nearestStaINNo
            nearestRailway = 2
        }else if(nearestStaSDis < nearestStaKODis && nearestStaSDis < nearestStaINDis){
            nearestStaDistance = if(nearestStaSDis < 1000.toDouble()) nearestStaSDis.toInt().toString() + "m"
            else "%,.1f".format(nearestStaSDis/1000) + "km"
            nearestStaNo = nearestStaSNo
            nearestRailway = 3
        }

    }
    private fun nearestStation(){
        //Log.d(TAG, "Location: $nowLat,$nowLon")
        var distance = 1000000000000.0f
        val results = FloatArray(3)
        if(railway==1){
            //Log.d(TAG, "nearestStation: "+staLon.size)
            for(i in staLon.indices){
                Location.distanceBetween(nowLat, nowLon, staLat[i], staLon[i], results)
                //Log.d(TAG, "nearestStation: $i : " + results[0])
                if(distance > results[0]){
                    distance = results[0]
                    nearestStaDistance = if(distance < 1000.toDouble()) distance.toInt().toString() + "m"
                    else "%,.1f".format(distance/1000) + "km"
                    nearestStaNo = i
                }
            }
        }
        else if(railway==2){
            //Log.d(TAG, "nearestStation: "+staLonIno.size)
            for(i in staLonIno.indices){
                Location.distanceBetween(nowLat, nowLon, staLatIno[i], staLonIno[i], results)
                //Log.d(TAG, "nearestStation: $i : " + results[0])
                if(distance > results[0]){
                    distance = results[0]
                    nearestStaDistance = if(distance < 1000.toDouble()) distance.toInt().toString() + "m"
                    else "%,.1f".format(distance/1000) + "km"
                    nearestStaNo = i
                }
            }
        }
        else if(railway==3){
            //Log.d(TAG, "nearestStation: "+staLonToei.size)
            for(i in staLonToei.indices){
                Location.distanceBetween(nowLat, nowLon, staLatToei[i], staLonToei[i], results)
                //Log.d(TAG, "nearestStation: $i : " + results[0])
                if(distance > results[0]){
                    distance = results[0]
                    nearestStaDistance = if(distance < 1000.toDouble()) distance.toInt().toString() + "m"
                    else "%,.1f".format(distance/1000) + "km"
                    nearestStaNo = i
                }
            }
        }
    }
    @SuppressLint("UnspecifiedImmutableFlag", "SetTextI18n", "ClickableViewAccessibility")
    private fun init(){
        val handler = Handler(Looper.getMainLooper())
        val timerText: TextView = findViewById(R.id.timer)
        timer(name = "testTimer", period = 1000) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
            val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
            handler.post {
                timerText.text = "$hour:$minute"
                //getLocation()
            }
        }
        settingNowPage = 0
        //page = 1
        pushButton()
        val posMark: TextView = findViewById(R.id.pos)
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val intVal = sharedPref.getBoolean("Page1", false)
        //posMark.text="。．．．．"
        val changeButton: Button = findViewById(R.id.change)
        changeButton.setOnClickListener {
            settingNow = true
            listUpdate0()
        }
        //if(intVal)listSet()
        //else listUpdate0()
        staLocationSet()
        val rightImg: ImageView = findViewById(R.id.right)
        val leftImg: ImageView = findViewById(R.id.left)
        val sizeX = getDisplaySize(this).x.toFloat()
        val sizeY = getDisplaySize(this).y.toFloat()
        rightImg.y = sizeY.div(2)
        leftImg.y = sizeY.div(2)
        rightImg.x = rightImg.width * -1f
        leftImg.x = sizeX + leftImg.width
        rightImg.isVisible = false
        leftImg.isVisible = false
        mDetector = GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean {
                startSwipe = event.x
                //Log.d(TAG, " onDown: $startSwipe")
                return false
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float,
                distanceY: Float
            ): Boolean {
                swipe(distanceX, e2.x,true)
                return false
            }
        })
        gestureListener = View.OnTouchListener { _, event ->
            if (event != null) {
                if(event.action == MotionEvent.ACTION_UP){
                    swipe(0f,0f,false)
                }
            }
            mDetector.onTouchEvent(event)
        }
        val listView: ListView = findViewById(R.id.listView)
        listView.setOnTouchListener(gestureListener)
    }
    private fun getLocation(){
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val intVal = sharedPref.getBoolean("Page1", false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),101)
            isLocationPermission = !(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)

        }else{
            isLocationPermission = true
        }
        if(isLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    //Log.d(TAG, "Location: ${location.latitude},${location.longitude}")
                    nowLat = location.latitude
                    nowLon = location.longitude
                    isNearestSta = true

                    //allNearestStation()
                    //nearestStation()
                }
                if(intVal)listSet()
                else listUpdate0()
            }
        }
        else{
            if(intVal)listSet()
            else listUpdate0()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val intVal = sharedPref.getBoolean("Page1", false)
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    isLocationPermission = true
                    getLocation()
                } else {
                    if(intVal)listSet()
                    else listUpdate0()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
    private fun numberRingSet(){
        numberRing.add(0)
        numberRing.add(R.drawable.keio_37)
        numberRing.add(R.drawable.keio_01)
        numberRing.add(R.drawable.keio_02)
        numberRing.add(R.drawable.keio_03)
        numberRing.add(R.drawable.keio_04)
        numberRing.add(R.drawable.keio_05)
        numberRing.add(R.drawable.keio_06)
        numberRing.add(R.drawable.keio_07)
        numberRing.add(R.drawable.keio_08)
        numberRing.add(R.drawable.keio_09)
        numberRing.add(R.drawable.keio_10)
        numberRing.add(R.drawable.keio_11)
        numberRing.add(R.drawable.keio_12)
        numberRing.add(R.drawable.keio_13)
        numberRing.add(R.drawable.keio_14)
        numberRing.add(R.drawable.keio_15)
        numberRing.add(R.drawable.keio_16)
        numberRing.add(R.drawable.keio_17)
        numberRing.add(R.drawable.keio_18)
        numberRing.add(R.drawable.keio_19)
        numberRing.add(R.drawable.keio_20)
        numberRing.add(R.drawable.keio_21)
        numberRing.add(R.drawable.keio_22)
        numberRing.add(R.drawable.keio_23)
        numberRing.add(R.drawable.keio_24)
        numberRing.add(R.drawable.keio_25)
        numberRing.add(R.drawable.keio_26)
        numberRing.add(R.drawable.keio_27)
        numberRing.add(R.drawable.keio_28)
        numberRing.add(R.drawable.keio_29)
        numberRing.add(R.drawable.keio_30)
        numberRing.add(R.drawable.keio_31)
        numberRing.add(R.drawable.keio_32)
        numberRing.add(R.drawable.keio_33)
        numberRing.add(R.drawable.keio_34)
        numberRing.add(R.drawable.keio_35)
        numberRing.add(R.drawable.keio_36)
        numberRing.add(R.drawable.keio_37)
        numberRing.add(R.drawable.keio_38)
        numberRing.add(R.drawable.keio_39)
        numberRing.add(R.drawable.keio_40)
        numberRing.add(R.drawable.keio_41)
        numberRing.add(R.drawable.keio_42)
        numberRing.add(R.drawable.keio_43)
        numberRing.add(R.drawable.keio_44)
        numberRing.add(R.drawable.keio_45)
        numberRing.add(R.drawable.keio_46)
        numberRing.add(R.drawable.keio_47)
        numberRing.add(R.drawable.keio_48)
        numberRing.add(R.drawable.keio_49)
        numberRing.add(R.drawable.keio_50)
        numberRing.add(R.drawable.keio_51)
        numberRing.add(R.drawable.keio_52)
        numberRing.add(R.drawable.keio_53)
        numberRing.add(0)
        numberRing.add(0)
    }
    private fun numberRingInoSet(){
        numberRingIno.add(0)
        numberRingIno.add(R.drawable.inokashira_10)
        numberRingIno.add(R.drawable.inokashira_01)
        numberRingIno.add(R.drawable.inokashira_02)
        numberRingIno.add(R.drawable.inokashira_03)
        numberRingIno.add(R.drawable.inokashira_04)
        numberRingIno.add(R.drawable.inokashira_05)
        numberRingIno.add(R.drawable.inokashira_06)
        numberRingIno.add(R.drawable.inokashira_07)
        numberRingIno.add(R.drawable.inokashira_08)
        numberRingIno.add(R.drawable.inokashira_09)
        numberRingIno.add(R.drawable.inokashira_10)
        numberRingIno.add(R.drawable.inokashira_11)
        numberRingIno.add(R.drawable.inokashira_12)
        numberRingIno.add(R.drawable.inokashira_13)
        numberRingIno.add(R.drawable.inokashira_14)
        numberRingIno.add(R.drawable.inokashira_15)
        numberRingIno.add(R.drawable.inokashira_16)
        numberRingIno.add(R.drawable.inokashira_17)
        numberRingIno.add(0)
        numberRingIno.add(0)
    }
    private fun numberRungTieiSet(){
        numberRingToei.add(0)
        numberRingToei.add(R.drawable.toei_10)
        numberRingToei.add(R.drawable.toei_01)
        numberRingToei.add(R.drawable.toei_02)
        numberRingToei.add(R.drawable.toei_03)
        numberRingToei.add(R.drawable.toei_04)
        numberRingToei.add(R.drawable.toei_05)
        numberRingToei.add(R.drawable.toei_06)
        numberRingToei.add(R.drawable.toei_07)
        numberRingToei.add(R.drawable.toei_08)
        numberRingToei.add(R.drawable.toei_09)
        numberRingToei.add(R.drawable.toei_10)
        numberRingToei.add(R.drawable.toei_11)
        numberRingToei.add(R.drawable.toei_12)
        numberRingToei.add(R.drawable.toei_13)
        numberRingToei.add(R.drawable.toei_14)
        numberRingToei.add(R.drawable.toei_15)
        numberRingToei.add(R.drawable.toei_16)
        numberRingToei.add(R.drawable.toei_17)
        numberRingToei.add(R.drawable.toei_18)
        numberRingToei.add(R.drawable.toei_19)
        numberRingToei.add(R.drawable.toei_20)
        numberRingToei.add(R.drawable.toei_21)
        numberRingToei.add(0)
        numberRingToei.add(0)
        
    }
    private fun reader(context: Context, filePath: String) {
        val assetManager: AssetManager = context.resources.assets
        try {
            //Log.d(TAG,"ok")
            // CSVファイルの読み込み
            val cr = contentResolver
            //val inputStream: InputStream = assetManager.open(filePath)Paths.get(this.filesDir.path+"/timetable/data.zip")
            val inputStream: InputStream? = cr.openInputStream(File(filesDir.path+"/timetable/"+filePath).toUri())
            //Log.d(TAG,inputStream.toString())
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferReader = BufferedReader(inputStreamReader)
            for (i in 1..2) {
                time.add("")
                type.add(0)
                bound.add("")
            }
            bufferReader.forEachLine {
                val rowData = it.split(",").toTypedArray()
                //CSVの左([0]番目)から順番にセット
                time.add(rowData[0])
                //Log.d("reader",rowData[1])
                when (rowData[1]) {
                    "京王ライナー" -> type.add(R.drawable.keioliner)
                    "Mt.TAKAO号" -> type.add(R.drawable.mt_takao)
                    "特急" -> type.add(R.drawable.specialexpress)
                    "急行" -> type.add(R.drawable.express)
                    "区間急行" -> type.add(R.drawable.semiexpress)
                    "快速" -> type.add(R.drawable.rapid)
                    "各駅停車" -> type.add(R.drawable.keiolocal)
                    "普通" -> type.add(R.drawable.toeilocal)
                }
                if (rowData[2].length == 2) bound.add(rowData[2].substring(0, 1) + "　" + rowData[2].substring(1, 2)) else bound.add(rowData[2])

            }
            for (i in 1..2) {
                time.add("")
                type.add(0)
                bound.add("")
            }
            bufferReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private var listUpdate0Pos = 0
    private fun listUpdate0(){
        settingNowPage = 5
        allNearestStation()
        val stnView: TextView = findViewById(R.id.staname)
        val day: TextView = findViewById(R.id.day)
        val listView: ListView = findViewById(R.id.listView)
        val changeButton: Button = findViewById(R.id.change)
        val baseNumberRing : ArrayList<Int> = ArrayList()
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val intVal = sharedPref.getBoolean("Page1", false)
        val intVal2 = sharedPref.getBoolean("Page2", false)
        val intVal3 = sharedPref.getBoolean("Page3", false)
        val intVal4 = sharedPref.getBoolean("Page4", false)
        val intVal5 = sharedPref.getBoolean("Page5", false)

        baseNumberRing.add(0)
        baseNumberRing.add(R.drawable.keio_37)
        baseNumberRing.add(R.drawable.keio)
        baseNumberRing.add(R.drawable.inokashira)
        baseNumberRing.add(R.drawable.toei)
        if(page == 1 && intVal || page == 2 && intVal2 || page == 3 && intVal3 || page == 4 && intVal4 || page == 5 && intVal5)baseNumberRing.add(0)
        baseNumberRing.add(0)
        changeButton.setOnClickListener {
            settingNow = true
            this.listUpdate0()
        }
        var sn: Array<String> = arrayOf()
        sn += ""
        sn += ""
        //Log.d(TAG,"isNearestSta = $isNearestSta")
        if(isNearestSta){
            if(nearestRailway == 1) {
                sn[1] =
                    ("現在地\n(${resources.getStringArray(R.array.STA_NAME)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                baseNumberRing[1] = numberRing[nearestStaNo + 2]
            }
            else if(nearestRailway == 2) {
                sn[1] =
                    ("現在地\n(${resources.getStringArray(R.array.STA_NAME_INO)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                baseNumberRing[1] = numberRingIno[nearestStaNo + 2]
            }
            else if(nearestRailway == 3) {
                sn[1] =
                    ("現在地\n(${resources.getStringArray(R.array.STA_NAME_TOEI)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                baseNumberRing[1] = numberRingToei[nearestStaNo + 2]
            }
        }
        else{
            baseNumberRing[1] = android.R.drawable.presence_offline
            sn[1] = "現在地\n(取得できませんでした)"
        }
        //Log.d(TAG, "listUpdate: KO-"+(nearestStaNo + 1))
        sn += "京王線"
        sn += "井の頭線"
        sn += "都営新宿線"
        if(page == 1 && intVal || page == 2 && intVal2 || page == 3 && intVal3 || page == 4 && intVal4 || page == 5 && intVal5)sn += "戻る"
        sn += ""
        val adapter2: BaseAdapter = MyAdapter2(
            this.applicationContext,
            R.layout.stationlist_layout,
            baseNumberRing, sn
        )
        //val adapter2: ArrayAdapter<*> =
        //ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn)
        day.visibility = View.INVISIBLE
        stnView.text = "路線を選択してください。"
        listView.adapter = adapter2
        listView.requestFocus()
        listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                if(position==sn.size-2&&(page == 1 && intVal || page == 2 && intVal2 || page == 3 && intVal3 || page == 4 && intVal4 || page == 5 && intVal5)){
                    pushButton()
                }else if((position == 1 && isNearestSta) ||position == 2||position == 3||position == 4) {
                    listUpdate0Pos = position
                    listUpdate()
                }
            }

    }
    private fun listUpdate() {
        settingNowPage = 1
        when (listUpdate0Pos) {
            1 -> {

                listUpdate5()
            }
            2 -> {
                railway = 1
                nearestStation()
                //Log.d(TAG, "{settingNow}")
                val stnView: TextView = findViewById(R.id.staname)
                val day: TextView = findViewById(R.id.day)
                val listView: ListView = findViewById(R.id.listView)
                val changeButton: Button = findViewById(R.id.change)
                changeButton.setOnClickListener {
                    settingNow = true
                    this.listUpdate0()
                }
                var sn: Array<String> = arrayOf()
                sn += ""
                sn += resources.getStringArray(R.array.STA_NAME)
                if (isNearestSta) {
                    sn[1] =
                        ("現在地\n(${resources.getStringArray(R.array.STA_NAME)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                    numberRing[1] = numberRing[nearestStaNo + 2]
                } else{
                    numberRing[1] = android.R.drawable.presence_offline
                    sn[1] = "現在地\n(取得できませんでした)"
                }
                //Log.d(TAG, "listUpdate: KO-" + (nearestStaNo + 1))
                sn += "戻る"
                sn += ""
                val adapter2: BaseAdapter = MyAdapter2(
                    this.applicationContext,
                    R.layout.stationlist_layout,
                    numberRing, sn
                )
                //val adapter2: ArrayAdapter<*> =
                //ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn)
                day.visibility = View.INVISIBLE
                stnView.text = "駅を選択してください。"
                listView.adapter = adapter2
                listView.requestFocus()
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position, _ ->
                        listUpdate2(position, sn, stnView, day, listView, changeButton)
                    }
            }
            3 -> {
                railway = 2
                nearestStation()
                //Log.d(TAG, "{settingNow}")
                val stnView: TextView = findViewById(R.id.staname)
                val day: TextView = findViewById(R.id.day)
                val listView: ListView = findViewById(R.id.listView)
                val changeButton: Button = findViewById(R.id.change)
                changeButton.setOnClickListener {
                    settingNow = true
                    this.listUpdate0()
                }
                var sn: Array<String> = arrayOf()
                sn += ""
                sn += resources.getStringArray(R.array.STA_NAME_INO)
                if (isNearestSta) {
                    sn[1] =
                        ("現在地\n(${resources.getStringArray(R.array.STA_NAME_INO)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                    numberRingIno[1] = numberRingIno[nearestStaNo + 2]
                } else{
                    numberRingIno[1] = android.R.drawable.presence_offline
                    sn[1] = "現在地\n(取得できませんでした)"
                }
                //Log.d(TAG, "listUpdate: KO-" + (nearestStaNo + 1))
                sn += "戻る"
                sn += ""
                val adapter2: BaseAdapter = MyAdapter2(
                    this.applicationContext,
                    R.layout.stationlist_layout,
                    numberRingIno, sn
                )
                //val adapter2: ArrayAdapter<*> =
                //ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn)
                day.visibility = View.INVISIBLE
                stnView.text = "駅を選択してください。"
                listView.adapter = adapter2
                listView.requestFocus()
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position, _ ->
                        listUpdate2(position, sn, stnView, day, listView, changeButton)
                    }
            }
            4 -> {
                railway = 3
                nearestStation()
                //Log.d(TAG, "{settingNow}")
                val stnView: TextView = findViewById(R.id.staname)
                val day: TextView = findViewById(R.id.day)
                val listView: ListView = findViewById(R.id.listView)
                val changeButton: Button = findViewById(R.id.change)
                changeButton.setOnClickListener {
                    settingNow = true
                    this.listUpdate0()
                }
                var sn: Array<String> = arrayOf()
                sn += ""
                sn += resources.getStringArray(R.array.STA_NAME_TOEI)
                if (isNearestSta) {
                    sn[1] =
                        ("現在地\n(${resources.getStringArray(R.array.STA_NAME_TOEI)[nearestStaNo + 1]}\n　　:直線距離 $nearestStaDistance)")
                    numberRingToei[1] = numberRingToei[nearestStaNo + 2]
                } else{
                    numberRingToei[1] = android.R.drawable.presence_offline
                    sn[1] = "現在地\n(取得できませんでした)"
                }
                //Log.d(TAG, "listUpdate: KO-" + (nearestStaNo + 1))
                sn += "戻る"
                sn += ""
                val adapter2: BaseAdapter = MyAdapter2(
                    this.applicationContext,
                    R.layout.stationlist_layout,
                    numberRingToei, sn
                )
                //val adapter2: ArrayAdapter<*> =
                //ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn)
                day.visibility = View.INVISIBLE
                stnView.text = "駅を選択してください。"
                listView.adapter = adapter2
                listView.requestFocus()
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position, _ ->
                        listUpdate2(position, sn, stnView, day, listView, changeButton)
                    }
            }
        }
    }
    private fun listUpdate2(position: Int,sn: Array<String>,stnView: TextView,day: TextView,listView: ListView,changeButton: Button){
        lu2 = Lang(position,sn,stnView,day,listView,changeButton)
        settingNowPage = 2
        if(railway == 1) {
            if (position != 0 && position != sn.size - 1 && position != sn.size - 2 && (position == 1 && isNearestSta || position != 1)) {
                day.text = ""
                var stn: String = "KO" + (nearestStaNo + 1).toString().padStart(2, '0')
                if (position != 1) stn = "KO" + (position - 1).toString().padStart(2, '0')
                if (Station.valueOf(stn).staName.length == 2) (Station.valueOf(stn).staName.substring(
                    0,
                    1
                ) + "　" + Station.valueOf(stn).staName.substring(1, 2)).also {
                    stnView.text = it
                } else stnView.text = Station.valueOf(stn).staName
                var sn2: Array<String> = resources.getStringArray(Station.valueOf(stn).staInfo)
                sn2 += "戻る"
                sn2 += ""
                val adapter3: ArrayAdapter<*> =
                    ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
                listView.adapter = adapter3
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position2, _ ->
                        if (position2 == sn2.size - 2) listUpdate() else listUpdate3(
                            position2,
                            position,
                            sn2,
                            stn,
                            day,
                            listView,
                            changeButton,
                            sn,
                            stnView
                        )
                    }
            } else if (position == sn.size - 2) {
                listUpdate0()
            }
        }
        else if(railway == 2) {
            if (position != 0 && position != sn.size - 1 && position != sn.size - 2 && (position == 1 && isNearestSta || position != 1)) {
                day.text = ""
                var stn: String = "IN" + (nearestStaNo + 1).toString().padStart(2, '0')
                if (position != 1) stn = "IN" + (position - 1).toString().padStart(2, '0')
                if (StationIno.valueOf(stn).staName.length == 2) (StationIno.valueOf(stn).staName.substring(
                    0,
                    1
                ) + "　" + StationIno.valueOf(stn).staName.substring(1, 2)).also {
                    stnView.text = it
                } else stnView.text = StationIno.valueOf(stn).staName
                var sn2: Array<String> = resources.getStringArray(StationIno.valueOf(stn).staInfo)
                sn2 += "戻る"
                sn2 += ""
                val adapter3: ArrayAdapter<*> =
                    ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
                listView.adapter = adapter3
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position2, _ ->
                        if (position2 == sn2.size - 2) listUpdate() else listUpdate3(
                            position2,
                            position,
                            sn2,
                            stn,
                            day,
                            listView,
                            changeButton,
                            sn,
                            stnView
                        )
                    }
            } else if (position == sn.size - 2) {
                listUpdate0()
            }
        }
        else if(railway == 3) {
            if (position != 0 && position != sn.size - 1 && position != sn.size - 2 && (position == 1 && isNearestSta || position != 1)) {
                day.text = ""
                var stn: String = "S" + (nearestStaNo + 1).toString().padStart(2, '0')
                if (position != 1) stn = "S" + (position - 1).toString().padStart(2, '0')
                if (StationToei.valueOf(stn).staName.length == 2) (StationToei.valueOf(stn).staName.substring(
                    0,
                    1
                ) + "　" + StationToei.valueOf(stn).staName.substring(1, 2)).also {
                    stnView.text = it
                } else stnView.text = StationToei.valueOf(stn).staName
                var sn2: Array<String> = resources.getStringArray(StationToei.valueOf(stn).staInfo)
                sn2 += "戻る"
                sn2 += ""
                val adapter3: ArrayAdapter<*> =
                    ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
                listView.adapter = adapter3
                listView.onItemClickListener =
                    OnItemClickListener { _, _, position2, _ ->
                        if (position2 == sn2.size - 2) listUpdate() else listUpdate3(
                            position2,
                            position,
                            sn2,
                            stn,
                            day,
                            listView,
                            changeButton,
                            sn,
                            stnView
                        )
                    }
            } else if (position == sn.size - 2) {
                listUpdate0()
            }
        }
    }
    private fun listUpdate3(position: Int,pos: Int,sn2: Array<String>,stn: String,day: TextView,listView: ListView,changeButton: Button,sn: Array<String>,stnView: TextView){
        lu3 = Lang2(position,pos,sn2,stn,day,listView,changeButton,sn,stnView)
        settingNowPage = 3
        if (position != 0 && position != sn2.size - 1) {
            day.text = sn2[position]
            day.visibility = View.VISIBLE
            val sn3 = ArrayList<String>()
            sn3.add("")
            sn3.add("平日")
            sn3.add("土休日")
            sn3.add("自動 (現在の時刻に合わせる)")
            sn3.add("戻る")
            sn3.add("")
            val adapter4: ArrayAdapter<*> =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, sn3)
            listView.adapter = adapter4
            listView.onItemClickListener =
                OnItemClickListener { _, _, position2, _ ->
                    if(position2==4) listUpdate2(pos, sn, stnView, day, listView, changeButton) else if(position2!=5&&position2!=0) listUpdate4(
                        position2,
                        position,
                        sn2,
                        stn,
                        day,
                        listView,
                        stnView
                    )
                }
        }
    }
    private fun listUpdate4(position2: Int,position: Int,sn2: Array<String>,stn: String,day: TextView,listView: ListView,stnView: TextView){
        settingNowPage = 4
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val editor = sharedPref.edit()
        val calendar = Calendar.getInstance()
        if(railway == 1){
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((Station.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((Station.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((Station.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((Station.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
        else if(railway == 2){
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationIno.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((StationIno.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationIno.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((StationIno.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
        else if(railway == 3){
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationToei.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((StationToei.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationToei.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((StationToei.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
    }
    private fun listUpdate5(){
        settingNowPage = 6
        val stnView: TextView = findViewById(R.id.staname)
        val day: TextView = findViewById(R.id.day)
        val listView: ListView = findViewById(R.id.listView)
        if(nearestRailway == 1) {
            day.text = ""
            val stn: String = "KO" + (nearestStaNo + 1).toString().padStart(2, '0')
            if (Station.valueOf(stn).staName.length == 2) (Station.valueOf(stn).staName.substring(
                0,
                1
            ) + "　" + Station.valueOf(stn).staName.substring(1, 2)).also {
                stnView.text = it
            } else stnView.text = Station.valueOf(stn).staName
            var sn2: Array<String> = resources.getStringArray(Station.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            val adapter3: ArrayAdapter<*> =
                ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
            listView.adapter = adapter3
            listView.onItemClickListener =
                OnItemClickListener { _, _, position2, _ ->
                    if (position2 == sn2.size - 2) listUpdate0() else listUpdate6(position2, sn2)
                }
        }
        else if(nearestRailway == 2) {
            day.text = ""
            val stn: String = "IN" + (nearestStaNo + 1).toString().padStart(2, '0')
            if (StationIno.valueOf(stn).staName.length == 2) (StationIno.valueOf(stn).staName.substring(
                0,
                1
            ) + "　" + StationIno.valueOf(stn).staName.substring(1, 2)).also {
                stnView.text = it
            } else stnView.text = StationIno.valueOf(stn).staName
            var sn2: Array<String> = resources.getStringArray(StationIno.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            val adapter3: ArrayAdapter<*> =
                ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
            listView.adapter = adapter3
            listView.onItemClickListener =
                OnItemClickListener { _, _, position2, _ ->
                    if (position2 == sn2.size - 2) listUpdate0() else listUpdate6(position2, sn2)
                }
        }
        else if(nearestRailway == 3) {
            day.text = ""
            val stn: String = "S" + (nearestStaNo + 1).toString().padStart(2, '0')
            if (StationToei.valueOf(stn).staName.length == 2) (StationToei.valueOf(stn).staName.substring(
                0,
                1
            ) + "　" + StationToei.valueOf(stn).staName.substring(1, 2)).also {
                stnView.text = it
            } else stnView.text = StationToei.valueOf(stn).staName
            var sn2: Array<String> = resources.getStringArray(StationToei.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            val adapter3: ArrayAdapter<*> =
                ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, sn2)
            listView.adapter = adapter3
            listView.onItemClickListener =
                OnItemClickListener { _, _, position2, _ ->
                    if (position2 == sn2.size - 2) listUpdate0() else listUpdate6(position2, sn2)
                }
        }
    }
    private fun listUpdate6(position: Int, sn2: Array<String>){
        settingNowPage = 7
        val day: TextView = findViewById(R.id.day)
        val listView: ListView = findViewById(R.id.listView)
        if (position != 0 && position != sn2.size - 1) {
            day.text = sn2[position]
            day.visibility = View.VISIBLE
            val sn3 = ArrayList<String>()
            sn3.add("")
            sn3.add("平日")
            sn3.add("土休日")
            sn3.add("自動 (現在の時刻に合わせる)")
            sn3.add("戻る")
            sn3.add("")
            val adapter4: ArrayAdapter<*> =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, sn3)
            listView.adapter = adapter4
            listView.onItemClickListener =
                OnItemClickListener { _, _, position2, _ ->
                    if(position2 == sn3.size - 2) listUpdate5() else if(position2 != 0 && position2 != sn3.size - 1) listUpdate7(position, position2)
                }
        }
    }
    private fun listUpdate7(position: Int, position2: Int){
        settingNowPage = 8

        val stnView: TextView = findViewById(R.id.staname)
        val day: TextView = findViewById(R.id.day)
        val listView: ListView = findViewById(R.id.listView)
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val editor = sharedPref.edit()
        val calendar = Calendar.getInstance()
        if(nearestRailway == 1){
            val stn: String = "KO" + (nearestStaNo + 1).toString().padStart(2, '0')
            var sn2: Array<String> = resources.getStringArray(Station.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((Station.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((Station.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((Station.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((Station.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(Station.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
        else if(nearestRailway == 2){
            val stn: String = "IN" + (nearestStaNo + 1).toString().padStart(2, '0')
            var sn2: Array<String> = resources.getStringArray(StationIno.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationIno.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((StationIno.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationIno.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((StationIno.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationIno.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
        else if(nearestRailway == 3){
            val stn: String = "S" + (nearestStaNo + 1).toString().padStart(2, '0')
            var sn2: Array<String> = resources.getStringArray(StationToei.valueOf(stn).staInfo)
            sn2 += "戻る"
            sn2 += ""
            if (position != 0) {
                var filePaths : Array<String> = arrayOf()
                time.clear()
                type.clear()
                bound.clear()
                var worh = false
                when(position2){
                    1->filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                    2->filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                    3->{
                        if(isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationToei.valueOf(stn).holiday))
                                    worh = true
                                }
                            }else{
                                filePaths = resources.getStringArray((StationToei.valueOf(stn).holiday))
                                worh = true
                            }
                        }else if(isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                            worh = true
                        }else if(!isHoliday() && yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).holiday)
                            worh = true
                            reader(this, filePaths[position])
                            if(time[time.size - 3].length == 4) {
                                if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                                    filePaths = resources.getStringArray((StationToei.valueOf(stn).weekday))
                                    worh = false
                                }
                            }else{
                                filePaths = resources.getStringArray((StationToei.valueOf(stn).weekday))
                                worh = false
                            }
                        }else if(!isHoliday() && !yesterdayDiffer()){
                            filePaths = resources.getStringArray(StationToei.valueOf(stn).weekday)
                        }
                    }
                }
                if (position2 == 1) (sn2[position] + "\n平日").also { day.text = it } else if (position2 == 2) (sn2[position] + "\n土休日").also { day.text = it } else if(worh) (sn2[position] + "\n土休日").also { day.text = it } else (sn2[position] + "\n平日").also { day.text = it }
                val filePath: String = filePaths[position]
                when(page) {
                    1 -> {
                        editor.putString("Page1file", filePath)
                        editor.putString("Page1filename1", stnView.text.toString())
                        editor.putString("Page1filename2", day.text.toString())
                        editor.putBoolean("Page1",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh",1)
                            }
                            2 -> {
                                editor.putInt("worh",2)
                            }
                            else -> {
                                editor.putInt("worh",3)
                            }
                        }
                    }
                    2 -> {
                        editor.putString("Page2file", filePath)
                        editor.putString("Page2filename1", stnView.text.toString())
                        editor.putString("Page2filename2", day.text.toString())
                        editor.putBoolean("Page2",true)
                        when (position2) {
                            1 -> {
                                editor.putInt("worh2",1)
                            }
                            2 -> {
                                editor.putInt("worh2",2)
                            }
                            else -> {
                                editor.putInt("worh2",3)
                            }
                        }
                    }
                    3 -> {
                        editor.putString("Page3file", filePath)
                        editor.putString("Page3filename1", stnView.text.toString())
                        editor.putString("Page3filename2", day.text.toString())
                        editor.putBoolean("Page3",true)
                        when (position2) {
                            1 -> editor.putInt("worh3",1)
                            2 -> editor.putInt("worh3", 2)
                            else -> editor.putInt("worh3", 3)
                        }
                    }
                    4 -> {
                        editor.putString("Page4file", filePath)
                        editor.putString("Page4filename1", stnView.text.toString())
                        editor.putString("Page4filename2", day.text.toString())
                        editor.putBoolean("Page4",true)
                        when (position2) {
                            1 -> editor.putInt("worh4",1)
                            2 -> editor.putInt("worh4",2)
                            else -> editor.putInt("worh4",3)
                        }
                    }
                    5 -> {
                        editor.putString("Page5file", filePath)
                        editor.putString("Page5filename1", stnView.text.toString())
                        editor.putString("Page5filename2", day.text.toString())
                        editor.putBoolean("Page5",true)
                        when (position2) {
                            1 -> editor.putInt("worh5",1)
                            2 -> editor.putInt("worh5",2)
                            else -> editor.putInt("worh5",3)
                        }
                    }
                }
                time.clear()
                type.clear()
                bound.clear()
                reader(this, filePath)
                val adapter: BaseAdapter = MyAdapter(
                    this.applicationContext,
                    R.layout.list_layout,
                    time, type, bound
                )
                val calendar = Calendar.getInstance()
                when(page){
                    1-> {
                        if (time[time.size - 3].length == 4) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 1).toInt(),
                                time[time.size - 3].substring(2, 4).toInt()
                            ).toString()
                        ) else if (time[time.size - 3].length == 5) editor.putString(
                            "lastTrain",
                            nextDay(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)+1,
                                calendar.get(Calendar.DAY_OF_MONTH),
                                time[time.size - 3].substring(0, 2).toInt(),
                                time[time.size - 3].substring(3, 5).toInt()
                            ).toString()
                        )
                        if(isHoliday()) editor.putBoolean("lastWeek",true) else editor.putBoolean("lastweek",false)
                    }
                    2-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain2",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek2",true)
                        }else{
                            editor.putBoolean("lastweek2",false)
                        }
                    }
                    3-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain3",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek3",true)
                        }else{
                            editor.putBoolean("lastweek3",false)
                        }
                    }
                    4-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain4",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek4",true)
                        }else{
                            editor.putBoolean("lastweek4",false)
                        }
                    }
                    5-> {
                        if (time[time.size - 3].length == 4) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 1).toInt(),
                                    time[time.size - 3].substring(2, 4).toInt()
                                ).toString()
                            )
                        } else if (time[time.size - 3].length == 5) {
                            editor.putString(
                                "lastTrain5",
                                nextDay(
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH)+1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    time[time.size - 3].substring(0, 2).toInt(),
                                    time[time.size - 3].substring(3, 5).toInt()
                                ).toString()
                            )
                        }
                        if(isHoliday()){
                            editor.putBoolean("lastWeek5",true)
                        }else{
                            editor.putBoolean("lastweek5",false)
                        }
                    }
                }
                editor.apply()
                if(time[time.size - 3].length==4){
                    nextDay(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH)+1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        time[time.size - 3].substring(0,1).toInt(),
                        time[time.size - 3].substring(2,4).toInt()
                    )
                }
                if ((isHoliday() && position2 == 2 )|| (!isHoliday() && position2 == 1)||position2 == 3) {
                    listLoad()
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                    day.requestFocus()
                    val handler = Handler(Looper.getMainLooper())
                    val runnable =
                        Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                            listView.setSelection(n)
                        }
                    handler.postDelayed(runnable, 100)
                }else{
                    listView.adapter = adapter
                    day.visibility = View.VISIBLE
                }
                listView.onItemClickListener =
                    OnItemClickListener { _, _, _, _ ->
                    }
                settingNowPage = 0
                settingNow = false
            }
        }
    }
    private fun isHoliday(): Boolean {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val today = "$month/$date"
        val holiday: ArrayList<String> = arrayListOf()
        holiday.add("1/1")
        holiday.add("1/10")
        holiday.add("2/11")
        holiday.add("2/23")
        holiday.add("3/21")
        holiday.add("4/29")
        holiday.add("5/3")
        holiday.add("5/4")
        holiday.add("5/5")
        holiday.add("7/18")
        holiday.add("8/11")
        holiday.add("9/19")
        holiday.add("9/23")
        holiday.add("10/10")
        holiday.add("11/3")
        holiday.add("11/23")

        // ①土日の判定
        val day = LocalDateTime.now().dayOfWeek
        for (i in holiday.indices) {
            if (holiday[i] == today) return true
        }
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
    }
    private fun yesterdayDiffer(): Boolean {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val holiday: ArrayList<String> = arrayListOf()
        holiday.add("1/1")
        holiday.add("1/10")
        holiday.add("2/11")
        holiday.add("2/23")
        holiday.add("3/21")
        holiday.add("4/29")
        holiday.add("5/3")
        holiday.add("5/4")
        holiday.add("5/5")
        holiday.add("7/18")
        holiday.add("8/11")
        holiday.add("9/19")
        holiday.add("9/23")
        holiday.add("10/10")
        holiday.add("11/3")
        holiday.add("11/23")
        val yesterday = yesterday(year, month, date, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)).month.toString() + "/" + yesterday(year, month, date, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)).dayOfMonth
        val day = yesterday(year, month, date, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE)).dayOfWeek
        val day2 = LocalDateTime.now().dayOfWeek
        for (i in holiday.indices) {
            if (holiday[i] == yesterday && !isHoliday() || holiday[i] != yesterday && isHoliday() && day != DayOfWeek.SUNDAY && day2 != DayOfWeek.SUNDAY) return true
        }
        return (!isHoliday() && day == DayOfWeek.SUNDAY)
    }
    private fun nextDay(y: Int, m: Int, d: Int, Hour: Int,Minute: Int): LocalDateTime {
        var year = y
        var month = m
        var day = d

        val calendar = Calendar.getInstance()
        when(calendar.get(Calendar.MONTH)+1){
            1 , 3 , 5 , 7 , 8 , 10-> {
                if (calendar.get(Calendar.DAY_OF_MONTH) == 31) {
                    month++
                    day = 1
                } else {
                    day++
                }
            }
            12-> {
                if (calendar.get(Calendar.DAY_OF_MONTH) == 31) {
                    year++
                    month = 1
                    day = 1
                } else {
                    day++
                }
            }
            4 , 6 , 9 , 11->{
                if(calendar.get(Calendar.DAY_OF_MONTH)==30){
                    month++
                    day = 1

                }else{
                    day++
                }
            }
            2-> {
                if (calendar.get(Calendar.YEAR) % 4 != 0 && calendar.get(Calendar.DAY_OF_MONTH) == 28) {
                    month++
                    day = 1
                } else if (calendar.get(Calendar.YEAR) % 4 == 0 && calendar.get(Calendar.DAY_OF_MONTH) == 29) {
                    month++
                    day = 1
                } else {
                    day++
                }
            }
        }
        return LocalDateTime.of(year,month,day,Hour,Minute)
    }
    private fun yesterday(y: Int, m: Int, d: Int, Hour: Int,Minute: Int): LocalDateTime {
        var year = y
        var month = m
        var day = d

        val calendar = Calendar.getInstance()
        when(calendar.get(Calendar.MONTH)+1){
            5 , 7 , 8 , 10 , 12-> {
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    month--
                    day = 30
                } else {
                    day--
                }
            }
            1-> {
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    year--
                    month = 12
                    day = 31
                } else {
                    day--
                }
            }
            2 , 4 , 6 , 9 , 11->{
                if(calendar.get(Calendar.DAY_OF_MONTH)==1){
                    month--
                    day = 31

                }else{
                    day--
                }
            }
            3-> {
                if (calendar.get(Calendar.YEAR) % 4 != 0 && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    month--
                    day = 28
                } else if (calendar.get(Calendar.YEAR) % 4 == 0 && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    month--
                    day = 29
                } else {
                    day--
                }
            }
        }
        return LocalDateTime.of(year,month,day,Hour,Minute)
    }
    private fun listSet(){
        val calendar = Calendar.getInstance()
        val listView :ListView = findViewById(R.id.listView)
        val stnView: TextView = findViewById(R.id.staname)
        val day: TextView = findViewById(R.id.day)
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val editor = sharedPref.edit()
        var filePath = sharedPref.getString("Page1file","")
        var filePath2 = sharedPref.getString("Page2file","")
        var filePath3 = sharedPref.getString("Page3file","")
        var filePath4 = sharedPref.getString("Page4file","")
        var filePath5 = sharedPref.getString("Page5file","")
        val filePathName1Page1 = sharedPref.getString("Page1filename1","")
        val filePathName1Page2 = sharedPref.getString("Page2filename1","")
        val filePathName1Page3 = sharedPref.getString("Page3filename1","")
        val filePathName1Page4 = sharedPref.getString("Page4filename1","")
        val filePathName1Page5 = sharedPref.getString("Page5filename1","")
        val filePathName2Page1 = sharedPref.getString("Page1filename2","")
        val filePathName2Page2 = sharedPref.getString("Page2filename2","")
        val filePathName2Page3 = sharedPref.getString("Page3filename2","")
        val filePathName2Page4 = sharedPref.getString("Page4filename2","")
        val filePathName2Page5 = sharedPref.getString("Page5filename2","")
        val worh = sharedPref.getInt("worh",1)
        val worh2 = sharedPref.getInt("worh2",1)
        val worh3 = sharedPref.getInt("worh3",1)
        val worh4 = sharedPref.getInt("worh4",1)
        val worh5 = sharedPref.getInt("worh5",1)
        var dateCheck = false
        time.clear()
        type.clear()
        bound.clear()
        when(page){
            1->{
                filePath?.let { reader(this, it) }
                if(time[time.size - 3].length == 4) {
                    if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                        dateCheck = true
                        if (worh == 3 && isHoliday() && filePath?.indexOf("weekday") != -1) filePath =
                            filePath?.replace("weekday", "holiday")
                        else if (worh == 3 && !isHoliday() && filePath?.indexOf("holiday") != -1) filePath =
                            filePath?.replace("holiday", "weekday")
                    }
                }else{
                    dateCheck = true
                    if (worh == 3 && isHoliday() && filePath?.indexOf("weekday") != -1) filePath =
                        filePath?.replace("weekday", "holiday")
                    else if (worh == 3 && !isHoliday() && filePath?.indexOf("holiday") != -1) filePath =
                        filePath?.replace("holiday", "weekday")
                }
                editor.putString("Page1file",filePath)
                time.clear()
                type.clear()
                bound.clear()
                filePath?.let { reader(this, it) }
            }
            2->{
                filePath2?.let { reader(this, it) }
                if(time[time.size - 3].length == 4) {
                    if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                        //Log.d(TAG, "listSet: ok")
                        dateCheck = true
                        if (worh2 == 3 && isHoliday() && filePath2?.indexOf("weekday") != -1) filePath2 =
                            filePath2?.replace("weekday", "holiday")
                        else if (worh2 == 3 && !isHoliday() && filePath2?.indexOf("holiday") != -1) filePath2 =
                            filePath2?.replace("holiday", "weekday")
                    }
                }else{
                    dateCheck = true
                    if (worh2 == 3 && isHoliday() && filePath2?.indexOf("weekday") != -1) filePath2 =
                        filePath2?.replace("weekday", "holiday")
                    else if (worh2 == 3 && !isHoliday() && filePath2?.indexOf("holiday") != -1) filePath2 =
                        filePath2?.replace("holiday", "weekday")
                }
                editor.putString("Page2file",filePath2)
                time.clear()
                type.clear()
                bound.clear()
                filePath2?.let { reader(this, it) }
            }
            3->{
                filePath3?.let { reader(this, it) }
                if(time[time.size - 3].length == 4) {
                    if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                        //Log.d(TAG, "listSet: ok")
                        dateCheck = true
                        if (worh3 == 3 && isHoliday() && filePath3?.indexOf("weekday") != -1) filePath3 =
                            filePath3?.replace("weekday", "holiday")
                        else if (worh3 == 3 && !isHoliday() && filePath3?.indexOf("holiday") != -1) filePath3 =
                            filePath3?.replace("holiday", "weekday")
                    }
                }else{
                    dateCheck = true
                    if (worh3 == 3 && isHoliday() && filePath3?.indexOf("weekday") != -1) filePath3 =
                        filePath3?.replace("weekday", "holiday")
                    else if (worh3 == 3 && !isHoliday() && filePath3?.indexOf("holiday") != -1) filePath3 =
                        filePath3?.replace("holiday", "weekday")
                }
                editor.putString("Page3file",filePath3)
                time.clear()
                type.clear()
                bound.clear()
                filePath3?.let { reader(this, it) }
            }
            4->{
                filePath4?.let { reader(this, it) }
                if(time[time.size - 3].length == 4) {
                    if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                        //Log.d(TAG, "listSet: ok")
                        dateCheck = true
                        if (worh4 == 3 && isHoliday() && filePath4?.indexOf("weekday") != -1) filePath4 =
                            filePath4?.replace("weekday", "holiday")
                        else if (worh4 == 3 && !isHoliday() && filePath4?.indexOf("holiday") != -1) filePath4 =
                            filePath4?.replace("holiday", "weekday")
                    }
                }else{
                    dateCheck = true
                    if (worh4 == 3 && isHoliday() && filePath4?.indexOf("weekday") != -1) filePath4 =
                        filePath4?.replace("weekday", "holiday")
                    else if (worh4 == 3 && !isHoliday() && filePath4?.indexOf("holiday") != -1) filePath4 =
                        filePath4?.replace("holiday", "weekday")
                }
                editor.putString("Page4file",filePath4)
                time.clear()
                type.clear()
                bound.clear()
                filePath4?.let { reader(this, it) }
            }
            5->{
                filePath5?.let { reader(this, it) }
                if(time[time.size - 3].length == 4) {
                    if (!(time[time.size - 3].substring(2,4).toInt() > calendar.get(Calendar.MINUTE) && time[time.size - 3].substring(0, 1).toInt() == calendar.get(Calendar.HOUR_OF_DAY))) {
                        //Log.d(TAG, "listSet: ok")
                        dateCheck = true
                        if (worh5 == 3 && isHoliday() && filePath5?.indexOf("weekday") != -1) filePath5 =
                            filePath5?.replace("weekday", "holiday")
                        else if (worh5 == 3 && !isHoliday() && filePath5?.indexOf("holiday") != -1) filePath5 =
                            filePath5?.replace("holiday", "weekday")
                    }
                }else{
                    dateCheck = true
                    if (worh5 == 3 && isHoliday() && filePath5?.indexOf("weekday") != -1) filePath5 =
                        filePath5?.replace("weekday", "holiday")
                    else if (worh5 == 3 && !isHoliday() && filePath5?.indexOf("holiday") != -1) filePath5 =
                        filePath5?.replace("holiday", "weekday")
                }
                editor.putString("Page5file",filePath5)
                time.clear()
                type.clear()
                bound.clear()
                filePath5?.let { reader(this, it) }
            }
        }
        val adapter: BaseAdapter = MyAdapter(
            this.applicationContext,
            R.layout.list_layout,
            time, type, bound
        )
        when(page){
            1->{
                if(time[time.size - 3].length == 4) editor.putString("lastTrain",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 1).toInt(),time[time.size - 3].substring(2, 4).toInt()).toString())
                else if(time[time.size - 3].length == 5) editor.putString("lastTrain",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 2).toInt(),time[time.size - 3].substring(3, 5).toInt()).toString())
                if(isHoliday())editor.putBoolean("lastWeek", true)
                else editor.putBoolean("lastWeek", false)
            }
            2->{
                if(time[time.size - 3].length == 4) editor.putString("lastTrain2",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 1).toInt(),time[time.size - 3].substring(2, 4).toInt()).toString())
                else if(time[time.size - 3].length == 5) editor.putString("lastTrain2",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 2).toInt(),time[time.size - 3].substring(3, 5).toInt()).toString())
                if(isHoliday())editor.putBoolean("lastWeek2", true)
                else editor.putBoolean("lastWeek2", false)
            }
            3->{
                if(time[time.size - 3].length == 4) editor.putString("lastTrain3",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 1).toInt(),time[time.size - 3].substring(2, 4).toInt()).toString())
                else if(time[time.size - 3].length == 5) editor.putString("lastTrain3",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 2).toInt(),time[time.size - 3].substring(3, 5).toInt()).toString())
                if(isHoliday())editor.putBoolean("lastWeek3", true)
                else editor.putBoolean("lastWeek3", false)
            }
            4->{
                if(time[time.size - 3].length == 4) editor.putString("lastTrain4",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 1).toInt(),time[time.size - 3].substring(2, 4).toInt()).toString())
                else if(time[time.size - 3].length == 5) editor.putString("lastTrain4",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 2).toInt(),time[time.size - 3].substring(3, 5).toInt()).toString())
                if(isHoliday())editor.putBoolean("lastWeek4", true)
                else editor.putBoolean("lastWeek4", false)
            }
            5->{
                if(time[time.size - 3].length == 4) editor.putString("lastTrain5",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 1).toInt(),time[time.size - 3].substring(2, 4).toInt()).toString())
                else if(time[time.size - 3].length == 5) editor.putString("lastTrain5",nextDay(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), time[time.size - 3].substring(0, 2).toInt(),time[time.size - 3].substring(3, 5).toInt()).toString())
                if(isHoliday())editor.putBoolean("lastWeek5", true)
                else editor.putBoolean("lastWeek5", false)
            }
        }
        n = 0
        if((isHoliday() && (page == 1 && worh == 2 || page == 2 && worh2 == 2 || page == 3 && worh3 == 2 || page == 4 && worh4 == 2 || page == 5 && worh5 == 2)) || (!isHoliday() && (page == 1 && worh == 1 || page == 2 && worh2 == 1 || page == 3 && worh3 == 1 || page == 4 && worh4 == 1 || page == 5 && worh5 == 1)) || (page == 1 && worh == 3 || page == 2 && worh2 == 3 || page == 3 && worh3 == 3 || page == 4 && worh4 == 3 || page == 5 && worh5 == 3)) listLoad()
        listView.adapter = adapter
        listView.requestFocus()
        listView.onItemClickListener =
            OnItemClickListener { _, _, _, _ ->
            }
        day.visibility = View.VISIBLE
        day.requestFocus()
        val handler = Handler(Looper.getMainLooper())
        val runnable =
            Runnable { //リストアイテムの総数-1（0番目から始まって最後のアイテム）にフォーカスさせる
                listView.setSelection(n)
            }
        handler.postDelayed(runnable, 100)
        when(page){
            1->{
                stnView.text = filePathName1Page1
                if(dateCheck && worh == 3 && isHoliday() && filePathName2Page1?.indexOf("平日") != -1) day.text = filePathName2Page1?.replace("平日","土休日")
                else if(dateCheck && worh == 3 && !isHoliday() && filePathName2Page1?.indexOf("土休日") != -1) day.text = filePathName2Page1?.replace("土休日","平日")
                else day.text = filePathName2Page1
                editor.putString("Page1filename2",day.text.toString())
                //Log.d(TAG, "listSet: $day")
            }
            2->{
                stnView.text = filePathName1Page2
                if(dateCheck && worh2 == 3 && isHoliday() && filePathName2Page2?.indexOf("平日") != -1) day.text = filePathName2Page2?.replace("平日","土休日")
                else if(dateCheck && worh2 == 3 && !isHoliday() && filePathName2Page2?.indexOf("土休日") != -1) day.text = filePathName2Page2?.replace("土休日","平日")
                else day.text = filePathName2Page2
                editor.putString("Page1filename2_2",day.text.toString())
            }
            3->{
                stnView.text = filePathName1Page3
                if(dateCheck && worh3 == 3 && isHoliday() && filePathName2Page3?.indexOf("平日") != -1) day.text = filePathName2Page3?.replace("平日","土休日")
                else if(dateCheck && worh3 == 3 && !isHoliday() && filePathName2Page3?.indexOf("土休日") != -1) day.text = filePathName2Page3?.replace("土休日","平日")
                else day.text = filePathName2Page3
                editor.putString("Page1filename2_3",day.text.toString())
            }
            4->{
                stnView.text = filePathName1Page4
                if(dateCheck && worh4 == 3 && isHoliday() && filePathName2Page4?.indexOf("平日") != -1) day.text = filePathName2Page4?.replace("平日","土休日")
                else if(dateCheck && worh4 == 3 && !isHoliday() && filePathName2Page4?.indexOf("土休日") != -1) day.text = filePathName2Page4?.replace("土休日","平日")
                else day.text = filePathName2Page4
                editor.putString("Page1filename2_4",day.text.toString())
            }
            5->{
                stnView.text = filePathName1Page5
                if(dateCheck && worh5 == 3 && isHoliday() && filePathName2Page5?.indexOf("平日") != -1) day.text = filePathName2Page5?.replace("平日","土休日")
                else if(dateCheck && worh5 == 3 && !isHoliday() && filePathName2Page5?.indexOf("土休日") != -1) day.text = filePathName2Page5?.replace("土休日","平日")
                else day.text = filePathName2Page5
                editor.putString("Page1filename2_5",day.text.toString())
            }
        }
        editor.apply()
    }
    private fun listLoad(){
        for (i in 2..time.size - 3) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val data1 = LocalDateTime.of(
                year,
                month,
                day,
                hour,
                minute
            )
            var data2: LocalDateTime
            if (hour == 0 && time[time.size - 3].substring(0, 1).toInt() == 0){
                if(time[i].substring(0, 1).toInt() == 0) {
                    data2 = LocalDateTime.of(
                        year,
                        month,
                        day,
                        time[i].substring(0, 1).toInt(),
                        time[i].substring(2, 4).toInt()
                    )
                    if (data1.isBefore(data2)) {
                        n = i - 2
                        break
                    }
                }

            }else if (hour != 0){
                if(time[time.size - 3].length == 5){
                    if(time[time.size - 3].substring(0,2).toInt() == hour && time[time.size - 3].substring(3,5).toInt() < minute) break
                }
                if (time[i].length == 4) {
                    data2 = LocalDateTime.of(
                        year,
                        month,
                        day,
                        time[i].substring(0, 1).toInt(),
                        time[i].substring(2, 4).toInt()
                    )
                    if (time[time.size - 3].length == 4 && time[i].substring(0, 1).toInt() == 0) {
                        data2 = nextDay(
                            year,
                            month,
                            day,
                            time[i].substring(0, 1).toInt(),
                            time[i].substring(2, 4).toInt()
                        )
                    }
                } else {

                    data2 = LocalDateTime.of(
                        year,
                        month,
                        day,
                        time[i].substring(0, 2).toInt(),
                        time[i].substring(3, 5).toInt()
                    )
                }
                if(data1.isBefore(data2)){
                    n = i - 2
                    break
                }

            }else{
                n = 0
                break
            }

        }
    }
    private fun pushButton() {
        settingNow = false
        settingNowPage = 0
        val posMark: TextView = findViewById(R.id.pos)
        val sharedPref = getSharedPreferences(prefFileName, MODE_PRIVATE)
        val intVal = sharedPref.getBoolean("Page1", false)
        val intVal2 = sharedPref.getBoolean("Page2", false)
        val intVal3 = sharedPref.getBoolean("Page3", false)
        val intVal4 = sharedPref.getBoolean("Page4", false)
        val intVal5 = sharedPref.getBoolean("Page5", false)
        when(page){
            1->{
                posMark.text = "。．．．．"
                if(intVal){
                    listSet()
                }else{
                    listUpdate0()
                }
            }
            2 -> {
                posMark.text = "．。．．．"
                if(intVal2){
                    listSet()
                }else{
                    listUpdate0()
                }
            }
            3 -> {
                posMark.text = "．．。．．"
                if(intVal3){
                    listSet()
                }else{
                    listUpdate0()
                }
            }
            4 -> {
                posMark.text = "．．．。．"
                if(intVal4){
                    listSet()
                }else{
                    listUpdate0()
                }
            }
            5 -> {
                posMark.text = "．．．．。"
                if(intVal5){
                    listSet()
                }else{
                    listUpdate0()
                }
            }
        }
    }
    private var lastRightPos = 0f
    private var lastLeftPos = 100000f
    private fun swipe(swipeX: Float, nowPos:Float, swipeNow: Boolean){
        val rightImg: ImageView = findViewById(R.id.right)
        val leftImg: ImageView = findViewById(R.id.left)
        val size = getDisplaySize(this).x.toFloat()
        if(swipeNow) {
            totalSwipe += swipeX
        }else{
            lastRightPos = 0f
            lastLeftPos = 100000f
            rightImg.isVisible = false
            leftImg.isVisible = false
            rightImg.x = rightImg.width * -1f
            leftImg.x = size + leftImg.width
            if(changeSwipeR){
                if(page!=5) {
                    settingNowPage = 0
                    settingNow = false
                    page += 1
                    pushButton()
                }
            }
            if(changeSwipeL){
                if(!settingNow) {
                    if (page != 1) {
                        page -= 1
                        pushButton()
                    } else {
                        this.finish()
                    }
                }else{
                    when (settingNowPage) {
                        1 -> {
                            listUpdate0()
                        }
                        2 -> {
                            listUpdate()
                        }
                        3 -> {
                            lu2?.let { listUpdate2(it.position,
                                lu2!!.sn, lu2!!.stnView, lu2!!.day, lu2!!.listView, lu2!!.changeButton) }
                        }
                        4 -> {
                            lu3?.let { listUpdate3(it.position,
                                lu3!!.pos,
                                lu3!!.sn2,
                                lu3!!.stn, lu3!!.day, lu3!!.listView, lu3!!.changeButton,lu3!!.sn,lu3!!.stnView) }
                        }
                        5 -> {
                            pushButton()
                        }
                        6 -> {
                            listUpdate0()
                        }
                        7 -> {
                            listUpdate5()
                        }
                    }
                }
            }
            totalSwipe = 0f
            changeSwipeR = false
            changeSwipeL = false
        }
        if(size.div(4) > startSwipe){
            ////Log.d(TAG, "x:$totalSwipe,size:${size},LEFT")
            if(rightImg.x <= rightImg.width.div(3)) {
                rightImg.x = MathUtils.clamp(rightImg.x - swipeX, rightImg.width * -1f, rightImg.width.div(3).toFloat())
                lastRightPos = nowPos
            }else if(nowPos <= lastRightPos){
                rightImg.x = MathUtils.clamp(rightImg.x - swipeX, rightImg.width * -1f, rightImg.width.div(3).toFloat())
                lastRightPos = nowPos
            }
            rightImg.isVisible = true
            changeSwipeL = size.div(4) < kotlin.math.abs(totalSwipe)
        }
        else if(size.div(4) * 3 < startSwipe){
            ////Log.d(TAG,"x:$totalSwipe,size:${size},RIGHT")
            if(leftImg.x >= size - leftImg.width - leftImg.width.div(3)) {
                leftImg.x = MathUtils.clamp(leftImg.x - swipeX, size - leftImg.width - leftImg.width.div(3), size + leftImg.width)
                lastLeftPos = nowPos
            }else if(nowPos >= lastLeftPos){
                leftImg.x = MathUtils.clamp(leftImg.x - swipeX, size - leftImg.width - leftImg.width.div(3), size + leftImg.width)
                lastLeftPos = nowPos
            }

            leftImg.isVisible = true
            changeSwipeR = size.div(4) < kotlin.math.abs(totalSwipe)
        }
        ////Log.d(TAG,"left:$changeSwipeL,right:$changeSwipeR")
    }
    private fun getDisplaySize(activity: Activity): Point {
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point
    }
}

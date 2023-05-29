package com.example.alfabet_01

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.google.android.material.textfield.TextInputEditText
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.geometry.SubpolylineHelper
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), Session.RouteListener {

    lateinit var mapview: MapView
    private lateinit var mapKit: MapKit
    private var lastCarPoint = Point(0.0, 0.0)
    private var currentLocation = Point(0.0, 0.0)
    private var modelList: ArrayList<Model> = ArrayList<Model>()
    private var launcherCam : ActivityResultLauncher<Intent>? = null
    private var launcherSettings : ActivityResultLauncher<Intent>? = null
    private var lastImgUri: Uri? = null
    private lateinit var tapLis: MapObjectTapListener
    private var rewriteDescription = false
    lateinit var mtRouter: PedestrianRouter
    lateinit var car: PlacemarkMapObject
    private lateinit var mapObjects: MapObjectCollection

    var isCarOnMap = false

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("b11ae79f-43cf-49cd-849a-d8f84e060c8a")
        MapKitFactory.setLocale("en_US")
        MapKitFactory.initialize(this)
        TransportFactory.initialize(this)
        val isFirstLaunch = intent.getBooleanExtra("key", false)
        setContentView(R.layout.activity_main)
        mapview = findViewById(R.id.mapview)
        mapview.map.move(CameraPosition(Point(56.838011, 60.597474), 11.0f, 0.0f, 0.0f))
        mapKit = MapKitFactory.getInstance()

        if (isFirstLaunch) checkPerm()
        else checkGpsOn()

        val location = mapKit.createUserLocationLayer(mapview.mapWindow)
        location.isVisible = true

        mapObjects = mapview.map.mapObjects.addCollection()

        launcherCam = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                findViewById<ImageView>(R.id.imageView7).setImageURI(lastImgUri)
                Log.d("Tag", "Image set")
            } else lastImgUri = null
        }

        launcherSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            Log.d("Tag", "Return from Settings")
            if (result.resultCode == RESULT_OK) {
                val modelListReverted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra("modelFromSet", ArrayList<Model>()::class.java)
                } else {
                    result.data?.getSerializableExtra("modelFromSet") as ArrayList<Model>?
                }
                if (!modelListReverted.isNullOrEmpty()) {
                    modelListReverted.reverse()
                    modelList = modelListReverted
                }
                else {
                    modelList.clear()
                    if (isCarOnMap) car.parent.remove(car)
                    Log.d("Tag", "modelList is clear")
                }
                saveModelList()
            }
        }

        tapLis = object : MapObjectTapListener{
            override fun onMapObjectTap(p0: MapObject, p1: Point): Boolean {
                //Log.d("Tag", "TUPTUTPTUP!!!!!")
                findViewById<ConstraintLayout>(R.id.constrDescrInput).visibility = View.VISIBLE
                findViewById<Button>(R.id.button3).setText(R.string.apply_changes)
                val noteText = modelList.last().text.toCharArray()
                findViewById<TextInputEditText>(R.id.descrTextInput).setText(noteText, 0, noteText.size)
                findViewById<ImageView>(R.id.imageView7).setImageURI(lastImgUri)
                rewriteDescription = true
                return true
            }

        }
        loadModeList()
        if (isDarkMode()) {
            Log.d("Tag", "Dark theme")
            mapview.map.isNightModeEnabled = true
        }
        createTimerFun()
    }

    override fun onStop () {
        saveModelList()
        super.onStop()
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onDestroy() {
        saveModelList()
        super.onDestroy()


    }
    private fun saveModelList() {
        val pref = getSharedPreferences("pref", MODE_PRIVATE).edit(commit = true) {
            this.putStringSet("savedData", modelListToStringSet())
        }
    }

    private fun createTimerFun() {
        val handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.start()

        val handler = Handler(handlerThread.looper)
        val delay = 5000 // 5 секунд

        val runnable = object : Runnable {
            override fun run() {
                //Log.d("Tag", "Прошло 5 секунд")
                handler.postDelayed(this, delay.toLong())
            }
        }

        handler.postDelayed(runnable, delay.toLong())
    }

    private fun modelListToStringSet(): MutableSet<String> {
        val stringSet = mutableSetOf<String>()
        for (i in modelList) {
            val modelToString = i.latitude.toString() + "|" + i.longitude.toString() + "|" + i.text + "|" + i.imgPath + "|" + formatDateToString(i.date)
            Log.d("Tag", modelToString)
            stringSet.add(modelToString)
        }
        Log.d("Tag", stringSet.size.toString())
        return stringSet
    }

    private fun loadModeList() {
        val modelListStringSet = getSharedPreferences("pref", MODE_PRIVATE).getStringSet("savedData", mutableSetOf<String>())
        if (!modelListStringSet.isNullOrEmpty()) {
            Log.d("Tag", modelListStringSet.size.toString())
            for (i in modelListStringSet) {
                val lines: List<String> = i.split("|")
                Log.d("Tag", lines.size.toString())
                if (lines.size != 5) continue
                val date = formatStringToDate(lines[4])!!
                val imgUriString = if (lines[3] == "null") null else lines[3]
                modelList.add(0,
                    Model(
                        lines[0].toDouble(),
                        lines[1].toDouble(),
                        lines[2],
                        imgUriString,
                        date,
                        SimpleDateFormat("dd.MM.yyyy\n" + "HH:mm", Locale.getDefault()).format(date)
                    )
                )
                if (!modelList.isNullOrEmpty()) {
                    val lastModel = modelList.last()
                    lastCarPoint = Point(lastModel.latitude, lastModel.longitude)
                    lastImgUri = if (!lastModel.imgPath.isNullOrEmpty()) Uri.parse(lastModel.imgPath) else null
                    addCarOnMap()
                }
            }
        }
    }

    private fun formatDateToString(date: Date, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun formatStringToDate(dateString: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun subscribeLocationUpdates () {
        var updateCounter = 0
        val time = System.currentTimeMillis() / 1000
        val locationListener = object : LocationListener {
            override fun onLocationUpdated(p0: Location) {
                currentLocation = p0.position
                updateCounter++
                Log.d("Tag", "Location updated!!! $updateCounter")
                Log.d("Tag", "Location time ${System.currentTimeMillis() / 1000 - time}")
            }

            override fun onLocationStatusUpdated(p0: LocationStatus) {

            }

        }
        mapKit.createLocationManager().subscribeForLocationUpdates(1.0, 10, 0.0, true, FilteringMode.OFF, locationListener)
    }

    private fun isDarkMode() : Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }



    //private fun getLocation() {
    //    val locMan = mapKit.createLocationManager()
    //    val locLis: LocationListener = object : LocationListener{
    //        override fun onLocationUpdated(p0: Location) {
    //            Log.d("Tag", "onLocationUpdated")
    //            currentLocation = p0.position
    //            mapview.map.move(CameraPosition(p0.position, 20.0f, 0.0f, 0.0f),
    //                Animation(Animation.Type.SMOOTH, 3f), null)
    //        }
    //        override fun onLocationStatusUpdated(p0: LocationStatus) {
    //            Log.d("Tag", "onStatusUpdated")
    //        }
    //    }
    //    locMan.requestSingleUpdate(locLis)
    //}

    private fun checkPerm() {
        var alertText = ""
        val permArray = arrayOf(arrayOf(android.Manifest.permission.CAMERA, getString(R.string.permission_camera_warning)),
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.permission_location_warning)))
        for(perm in permArray) {
            val permCheck = ContextCompat.checkSelfPermission(this, perm[0]) != PackageManager.PERMISSION_GRANTED
            if (permCheck) alertText += perm[1]
        }

        if (alertText.length > 1) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.functional_is_limited_text))
            builder.setMessage(alertText)
            builder.setPositiveButton("OK"){ _, _ ->
                Log.d("Tag", "Positive")
                checkGpsOn()
            }
            builder.show()
        } else checkGpsOn()
    }

    private fun checkLocationOn(): Boolean {
        val isAccessLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val locMan = getSystemService(LOCATION_SERVICE) as LocationManager
        val isLocationOn = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("Tag", "$isAccessLocation    $isLocationOn")
        return isAccessLocation and isLocationOn
    }

    private fun checkGpsOn() {
        val locMan = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("Tag", "Location is enable")
        }
        else {
            Log.d("Tag", "Location is disable")
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.Geolocation_is_disabled)
            builder.setMessage(R.string.Enable_geolocation_text)
            builder.setPositiveButton("OK"){ _, _ ->
                Log.d("Tag", "Positive")
            }

            builder.show()
        }

    }






    fun onClickSettings(view: View) {

        //с

        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("qwerty", modelList)
        launcherSettings?.launch(intent)
    }

    fun onClickHelp(view: View) {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }

    fun onClickGPS(view: View) {
        checkGpsOn()
        //getLocation()
        if (currentLocation.latitude == 0.0) {
            subscribeLocationUpdates()
        } else {
            mapview.map.move(CameraPosition(currentLocation, 20.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 3f), null)
        }

    }

    fun onClickParking(view: View) {
        setParkingImgVisibility(View.VISIBLE)
        if (checkLocationOn() and (currentLocation.latitude != 0.0)) mapview.map.move(CameraPosition(currentLocation, 20.0f, 0.0f, 0.0f), Animation(Animation.Type.SMOOTH, 3f), null)
    }

    fun onClickCompass(view: View) {
        if ((currentLocation.latitude != 0.0) and (lastCarPoint.latitude != 0.0)) {
            createRoute(currentLocation, lastCarPoint)
        }
    }

    fun onClickConfirm(view: View) {
        lastCarPoint = mapview.map.cameraPosition.target
        setParkingImgVisibility(View.GONE)
        findViewById<ConstraintLayout>(R.id.constrDescrInput).visibility = View.VISIBLE
        findViewById<TextInputEditText>(R.id.descrTextInput).setText("")
        findViewById<Button>(R.id.button3).setText(R.string.save)
        findViewById<ImageView>(R.id.imageView7).setImageResource(R.drawable.img_default_photo)
        addCarOnMap()
    }

    private fun addCarOnMap() {
        var carBitMap = BitmapFactory.decodeResource(resources, R.drawable.img_car_point)
        carBitMap = Bitmap.createScaledBitmap(carBitMap, 80, 80, false)
        if (isCarOnMap) {
            car.parent.remove(car)
        }
        val newCar = mapview.map.mapObjects.addPlacemark(lastCarPoint, ImageProvider.fromBitmap(carBitMap))
        newCar.addTapListener(tapLis)
        car = newCar
        isCarOnMap = true
    }

    fun onClickCancel(view: View) {
        setParkingImgVisibility(View.GONE)
    }

    private fun setParkingImgVisibility(visibility: Int) {
        findViewById<ImageView>(R.id.imageViewPin).visibility = visibility
        findViewById<ImageButton>(R.id.imageButtonConfirm).visibility = visibility
        findViewById<ImageButton>(R.id.imageButtonCancel).visibility = visibility
    }

    fun onClickSaveDescription(view: View) {
        val descriptionText = findViewById<TextInputEditText>(R.id.descrTextInput).text.toString()
        val lastImgPath = if (lastImgUri != null) lastImgUri!!.toString() else null
        if (rewriteDescription) {
            val lastID = modelList.lastIndex
            modelList[lastID].text = descriptionText
            modelList[lastID].imgPath = lastImgPath
            findViewById<ConstraintLayout>(R.id.constrDescrInput).visibility = View.GONE
            rewriteDescription = false
        } else {
            val datetime = Calendar.getInstance().time
            val dateString = SimpleDateFormat("dd.MM.yyyy\n" + "HH:mm", Locale.getDefault()).format(datetime)
            modelList.add(Model(lastCarPoint.latitude, lastCarPoint.longitude, descriptionText, lastImgPath, datetime, dateString))
            lastImgUri = null
            findViewById<ConstraintLayout>(R.id.constrDescrInput).visibility = View.GONE
            findViewById<ImageButton>(R.id.imageButton4).setImageResource(R.drawable.img_compass_active)
        }

    }

    fun onClickAddPhoto(view: View){
        Log.d("Tag", "onClickAddPhoto")
        val ass = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (ass) {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val milis = System.currentTimeMillis().toString()
            val file = File(filesDir, "picture_$milis.jpg")

            val extraOutput = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + "." + localClassName + ".provider",
                file
            )
            Log.d("Tag", extraOutput.path!!)
            Log.d("Tag", extraOutput.encodedPath!!)
            lastImgUri = extraOutput
            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, extraOutput)

            launcherCam!!.launch(takePicture)
        }
        else {
            Toast.makeText(this, R.string.toast_camera_perm, Toast.LENGTH_LONG).show()
        }
    }

    private fun createRoute(startPoint: Point, endPoint: Point) {
        mapview.map.mapObjects.remove(mapObjects)
        mapObjects = mapview.map.mapObjects.addCollection()
        val options = TimeOptions()
        val points = ArrayList<RequestPoint>()
        points.add(RequestPoint(startPoint, RequestPointType.WAYPOINT, null))
        points.add(RequestPoint(endPoint, RequestPointType.WAYPOINT, null))
        mtRouter = TransportFactory.getInstance().createPedestrianRouter()
        mtRouter.requestRoutes(points, options, this)
    }

    override fun onMasstransitRoutes(p0: MutableList<Route>) {

        // In this example we consider first alternative only
        if (p0.size > 0) {
            for (section in p0[0].sections) {
                drawSection(
                    SubpolylineHelper.subpolyline(p0[0].geometry, section.geometry)
                )
            }
        }
    }

    override fun onMasstransitRoutesError(p0: Error) {
    }

    private fun drawSection(geometry: Polyline) {
        val polylineMapObject = mapObjects.addPolyline(geometry)
        val colorInt = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) 0xFFBFFF00.toInt()
                        else 0xFF3381CC.toInt()
        polylineMapObject.setStrokeColor(colorInt)
    }

}
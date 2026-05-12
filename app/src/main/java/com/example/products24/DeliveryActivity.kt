package com.example.products24

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.products24.data.model.OrderDto
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.mapkit.search.Session as YandexSession
import kotlinx.coroutines.launch

class DeliveryActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchManager: SearchManager
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var drivingRouter: DrivingRouter

    private var order: OrderDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Инициализация карт до super.onCreate
        MapKitFactory.initialize(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        // Получаем данные заказа
        order = intent.getSerializableExtra("ORDER_DATA") as? OrderDto
        if (order == null) {
            finish()
            return
        }

        initViews()
        setupMap()
    }

    private fun initViews() {
        findViewById<TextView>(R.id.tvDeliveryCustomerName).text = order?.userName
        findViewById<TextView>(R.id.tvDeliveryAddress).text = order?.addressDelivery

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnCallCustomer).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order?.phoneNumber}"))
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnCompleteOrder).setOnClickListener {
            completeOrder()
        }
    }

    private fun setupMap() {
        mapView = findViewById(R.id.deliveryMapView)
        val map = mapView.mapWindow.map
        mapObjects = map.mapObjects.addCollection()
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)

        val startPoint = Point(51.818416, 55.115847) // Магазин
        val fullAddress = "Оренбург, ${order?.addressDelivery}"

        val searchOptions = SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 1
        }

        val searchListener = object : YandexSession.SearchListener {
            override fun onSearchResponse(response: Response) {
                val targetPoint = response.collection.children.firstOrNull()?.obj
                    ?.geometry?.firstOrNull()?.point

                if (targetPoint != null) {
                    buildRoute(startPoint, targetPoint)
                }
            }
            override fun onSearchError(error: com.yandex.runtime.Error) {
                Log.e("Search", "Error: $error")
            }
        }

        // Используем геометрию для поиска
        val searchPolygon = VisibleRegionUtils.toPolygon(map.visibleRegion)


        searchManager.submit(fullAddress, searchPolygon, searchOptions, searchListener)

        // Начальный зум на город
        map.move(CameraPosition(startPoint, 12.0f, 0.0f, 0.0f))
    }

    private fun buildRoute(start: Point, end: Point) {
        val requestPoints = listOf(
            RequestPoint(start, RequestPointType.WAYPOINT, null, null),
            RequestPoint(end, RequestPointType.WAYPOINT, null, null)
        )

        drivingRouter.requestRoutes(requestPoints, DrivingOptions(), VehicleOptions(), object : DrivingSession.DrivingRouteListener {
            override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                if (routes.isNotEmpty()) {
                    mapObjects.clear()
                    mapObjects.addPlacemark(start)
                    mapObjects.addPlacemark(end)

                    val polyline = mapObjects.addPolyline(routes[0].geometry)
                    polyline.setStrokeColor(Color.parseColor("#90ee90"))
                    polyline.strokeWidth = 5f

                    mapView.mapWindow.map.move(CameraPosition(end, 15.0f, 0.0f, 0.0f))
                }
            }
            override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                Toast.makeText(this@DeliveryActivity, "Ошибка маршрута", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun completeOrder() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.authApi().markAsComplete(order!!.orderID)
                if (response.isSuccessful) {
                    Toast.makeText(this@DeliveryActivity, "Заказ завершен!", Toast.LENGTH_SHORT).show()
                    finish() // Возвращаемся в список заказов
                }
            } catch (e: Exception) {
                Log.e("Delivery", "Error", e)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
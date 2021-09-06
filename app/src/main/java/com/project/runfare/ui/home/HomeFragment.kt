package com.project.runfare.ui.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.project.runfare.R
import com.project.runfare.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*
import java.math.RoundingMode
import java.text.DecimalFormat

class HomeFragment : Fragment(), SensorEventListener {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        loadData()
        resetSteps()

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running){
            totalSteps = event!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            val currentCal = roundDecimal(currentSteps*0.04)
            val currentMoney = roundDecimal(currentSteps*0.002)

            val textView: TextView = binding.tvStepsTaken
            val calView: TextView = binding.calorie
            val moneyView: TextView = binding.money
            if(currentSteps == 1000){
                progress.apply { setProgressWithAnimation(0f) }
                previousTotalSteps = totalSteps
            }
            textView.text = ("$currentSteps")
            calView.text = ("$currentCal")
            moneyView.text = ("$$currentMoney")
            progress.apply {
                setProgressWithAnimation(currentSteps.toFloat())
            }
        }
    }
    private fun roundDecimal(number: Double) : Double?{
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toDouble()
    }

    private fun resetSteps(){
        val textView: TextView = binding.tvStepsTaken
        val calView: TextView = binding.calorie
        val moneyView: TextView = binding.money
        textView.setOnClickListener {
            Toast.makeText(activity, "Long press to reset steps.", Toast.LENGTH_SHORT).show()
        }
        textView.setOnLongClickListener {
            previousTotalSteps = totalSteps
            textView.text = 0.toString()
            calView.text = "0.00"
            moneyView.text = "$0.00"
            progress.apply {
                setProgressWithAnimation(0f)
            }
            saveData()
            true
        }
    }

    private fun saveData(){
        val sharedPreferences = activity?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putFloat("key1", previousTotalSteps)
        editor?.apply()
    }
    private fun loadData(){
        val sharedPreferences = activity?.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences?.getFloat("key1", 0f)
        if (savedNumber != null) {
            previousTotalSteps = savedNumber
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if(stepSensor == null){
            Toast.makeText(activity, "No sensor detected on this device.", Toast.LENGTH_SHORT).show()
        } else{
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
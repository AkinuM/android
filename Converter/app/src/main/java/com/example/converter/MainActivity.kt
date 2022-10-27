package com.example.converter

import android.content.ClipData
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import fragments.DistanceFragment
import fragments.TimeFragment
import fragments.WeightFragment
import java.util.*

class MainActivity : AppCompatActivity() {

    private val distanceFragment = DistanceFragment()
    lateinit var bottom_navigation : BottomNavigationView
    private lateinit var clipboardManager: android.content.ClipboardManager
    lateinit var convertFrom: String
    lateinit var convertTo: String
    private var inputFrom: String = ""
    private var inputTo: String = ""
    private var isActiveInputFrom = false
    private lateinit var output: TextView
    private lateinit var input: EditText
    private var commaActivated = false
    private var vipActivated = false
    lateinit var clipData: ClipData
    private var savedCopy: String = ""
    private val convertibleValues:Map<String, Double> = mapOf(
        "M" to 1.0, "Cm" to 0.01,
        "Km" to 1000.0,

        "Kg" to 1.0, "g" to 0.001,
        "T" to 1000.0,

        "Sec" to 1.0, "Min" to 60.0,
        "Hour" to 3600.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(distanceFragment)
        setChooseMenu(R.array.distance)
        initInput()

        bottom_navigation = findViewById(R.id.bottom_navigation) as BottomNavigationView
        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.distance -> {
                    replaceFragment(DistanceFragment())
                    setChooseMenu(R.array.distance)
                    initInput()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.weight -> {
                    replaceFragment(WeightFragment())
                    setChooseMenu(R.array.weight)
                    initInput()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.time -> {
                    replaceFragment(TimeFragment())
                    setChooseMenu(R.array.time)
                    initInput()
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {return@setOnNavigationItemSelectedListener false}
            }
        }
        if(savedInstanceState != null){
            bottom_navigation.selectedItemId = savedInstanceState.getInt("opened_fragment")
        }
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    }

    private fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        }
    }

    private fun setChooseMenu(arr: Int){
        val arrayAdapter = ArrayAdapter.createFromResource(this, arr, android.R.layout.simple_spinner_item)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinnerFrom = findViewById<Spinner>(R.id.input_spinner)
        val spinnerTo = findViewById<Spinner>(R.id.output_spinner)

        spinnerFrom.adapter = arrayAdapter
        spinnerTo.adapter = arrayAdapter

        spinnerFrom.onItemSelectedListener = object : AdapterView
        .OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                convertFrom = spinnerFrom.selectedItem.toString()
                Log.i("CONVERTER", convertFrom)
                update()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                convertFrom = spinnerTo.selectedItem.toString()
            }
        }
        spinnerTo.onItemSelectedListener = object : AdapterView
        .OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                convertTo = spinnerTo.selectedItem.toString()
                update()
                Log.i("CONVERTER", convertTo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                convertTo = spinnerTo.selectedItem.toString()
            }
        }
    }

    private fun update(){
        if(inputFrom.isNotEmpty() && isActiveInputFrom){
            Formatter.BigDecimalLayoutForm.SCIENTIFIC
            val res = (inputFrom.toBigDecimal()) * (convertibleValues[convertFrom]!! / convertibleValues[convertTo]!!).toBigDecimal()
            inputTo = res.toPlainString()
//            if (!commaActivated) {
//                inputTo = StringBuilder(inputTo).deleteRange(inputTo.length - 1, inputTo.length).toString()
//            }
            output.setText(inputTo)
        }
        else{
            output.setText("")
        }

        Log.i("mko", "Count")
    }

    fun onDigit(view: View) {
        if(isActiveInputFrom){
            val res = (input as EditText).selectionStart
            if (inputFrom.length < 16 || (inputFrom.length == 16 && commaActivated)){
                if (inputFrom == "0"){
                    if(res != 0){
                        inputFrom = (view as Button).text.toString()
                        input.setText(inputFrom)
                        input.setSelection(inputFrom.length)
                    }
                    else{
                        inputFrom = StringBuilder(inputFrom).insert(res,(view as Button).text.toString()).toString()
                        input.setText(inputFrom)
                        (input as EditText).setSelection(res + 1)
                    }
                }
                else{
                    inputFrom = StringBuilder(inputFrom).insert(res,(view as Button).text.toString()).toString()
                    input.setText(inputFrom)
                    (input as EditText).setSelection(res + 1)
                }

            }
            else if(inputFrom.length == 17 && inputFrom.endsWith('0') && res!=17 && commaActivated){
                inputFrom = StringBuilder(inputFrom).deleteRange(16, 17).toString()
                inputFrom = StringBuilder(inputFrom).insert(res,(view as Button).text.toString()).toString()
                input.setText(inputFrom)
                (input as EditText).setSelection(res + 1)
                update()

            }
            else {
                Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
            }
        }
        update()
    }

    private fun forbidActions(mEditText: TextView){
        mEditText.customSelectionActionModeCallback = object : ActionMode.Callback{
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
                return true
            }

            override fun onDestroyActionMode(p0: ActionMode?) {}
        }
    }

    private fun initInput() {
        Log.i("CURENCY", "initINPUT")
        val editTextFrom = findViewById<EditText>(R.id.input_text)
        editTextFrom.hint = "0.0"
        forbidActions(editTextFrom)
        editTextFrom.setText("")
        editTextFrom.showSoftInputOnFocus = false;
        input = editTextFrom
        inputFrom = ""
        inputTo=""
        editTextFrom.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                isActiveInputFrom = true
                Log.i("CONVERTER", "Focus input")
            }
        }
        output = findViewById(R.id.output_text)
        output.setText("")

        isActiveInputFrom = true

        val tv = findViewById<View>(R.id.output_text) as TextView
        tv.movementMethod = ScrollingMovementMethod()
    }

    fun onZero(view: View){
        if(inputFrom.length<16 || (inputFrom.length == 16 && commaActivated)) {
            if (isActiveInputFrom) {
                val res = (input as EditText).selectionStart
                if (inputFrom.length == 0 || (res != 0 && !(inputFrom[0] == '0' && res == 1 ))) {
                    if ((view as Button).text.toString() == "00" && ((inputFrom.length<11 && inputFrom.length != 0) || (inputFrom.length == 11 && commaActivated))){
                        inputFrom = StringBuilder(inputFrom).insert(res, "00").toString()
                        input.setText(inputFrom)
                        (input as EditText).setSelection(res + 2)
                    }
                    else {
                        inputFrom = StringBuilder(inputFrom).insert(res, "0").toString()
                        input.setText(inputFrom)
                        (input as EditText).setSelection(res + 1)
                    }
                }
                else {
                    Toast.makeText(this, "bad zero", Toast.LENGTH_SHORT).show()
                }
            }
            update()
        }
        else {
            Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
        }
    }

    fun onDot(view: View) {
        if (isActiveInputFrom && !commaActivated) {
            val res = (input as EditText).selectionStart
            if (inputFrom.length < 13) {
                if (inputFrom.length == 0) {
                    inputFrom += "0."
                    input.setText(inputFrom)
                    input.setSelection(inputFrom.length)
                    commaActivated = true
                }
                else if (inputFrom.length == 16 && res == 16){
                    Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
                }
                else {
                    inputFrom = StringBuilder(inputFrom).insert(res, ".").toString()
                    input.setText(inputFrom)
                    (input as EditText).setSelection(res + 1)
                    commaActivated = true
                }
                update()

            }
            else {
                Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, "many dots", Toast.LENGTH_SHORT).show()
        }
    }

    fun AllClear(view: View) {
        inputFrom = ""
        input.setText(inputFrom)
        output.setText("")
        commaActivated = false
    }

    fun Clear(view: View) {

        val res = (input as EditText).selectionStart
        if(isActiveInputFrom){
            if(res != 0) {
                inputFrom = StringBuilder(inputFrom).deleteRange(res - 1, res).toString()
                input.setText(inputFrom)
                (input as EditText).setSelection(res - 1)

                if (!inputFrom.contains(".")) {
                    commaActivated = false
                }
            }
        }
        update()
    }

    fun swap(view: View) {
        if (vipActivated) {
            if (isActiveInputFrom) {
                if (inputFrom != "") {
                    val res = (input as EditText).selectionStart
                    var temp = output.text.toString()
                    if (temp.length>17 || (temp.length == 17 && !commaActivated)){
                        Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        inputFrom = temp
                        input.setText(inputFrom)
                        input.setSelection(res)
                    }
                    commaActivated = true
                    update()
                }
            }
        }
        else {
            Toast.makeText(this, "need VIP", Toast.LENGTH_SHORT).show()
        }
    }

    fun vipActivated(view:View){
        if(!vipActivated){
            vipActivated = true
            Toast.makeText(this, "ty for donation", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "you already have VIP", Toast.LENGTH_SHORT).show()
        }

    }

    fun copyTo(view: View){
        if(!vipActivated){
            Toast.makeText(this, "need VIP", Toast.LENGTH_SHORT).show()
        }
        else{
            val result = findViewById<TextView>(R.id.output_text).text.toString()
            if(result.isNotEmpty()){
                clipData = ClipData.newPlainText("saved",result)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, "copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyFrom(view: View){
        if(!vipActivated){
            Toast.makeText(this, "need VIP", Toast.LENGTH_SHORT).show()
        }
        else{
            val result = findViewById<EditText>(R.id.input_text).text.toString()
            if(result.isNotEmpty()){
                clipData = ClipData.newPlainText("saved",result)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, "copied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun pasteFrom(view:View){
        if(!vipActivated){
            Toast.makeText(this, "need VIP", Toast.LENGTH_SHORT).show()
        }
        else {
            val saved = clipboardManager.primaryClip
            val item = saved?.getItemAt(0)
            val boofData = item?.text.toString()
            val keywords = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".")

            val match = keywords.filter { it in boofData }

            if(boofData.length>17){
                Toast.makeText(this, "too big, senpai", Toast.LENGTH_SHORT).show()
            }
            else{
                var k = 0
                var letter = false
                for (char in boofData){
                    if (char == '.'){
                        k++
                    }
                }
                if (k == 1){
                    commaActivated = true
                }
                if(k >= 2 || (boofData.length == 17 && k == 0)){
                    Toast.makeText(this, "many dots", Toast.LENGTH_SHORT).show()
                }
                else {
                    for (char in boofData){
                        if (char.isLetter()){
                            letter = true
                        }
                    }
                    if (!letter) {
                        inputFrom = item?.text.toString()
                        input.setText(inputFrom)

                        isActiveInputFrom = true
                        (input as EditText).setSelection(inputFrom.length)
                        update()

                        Log.i("Paste", item?.text.toString())
                    }
                    else{
                        Toast.makeText(this, "letters are not allowed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        val sConvertFrom = findViewById<Spinner>(R.id.input_spinner)
        val sConvertTo = findViewById<Spinner>(R.id.output_spinner)

        sConvertFrom.setSelection(savedInstanceState.getInt("convertFrom"))
        sConvertTo.setSelection(savedInstanceState.getInt("convertTo"))

        convertFrom = sConvertFrom.selectedItem.toString()
        convertTo = sConvertTo.selectedItem.toString()

        inputFrom = savedInstanceState.getString("inputFrom").toString()


        inputTo = savedInstanceState.getString("inputTo").toString()

        vipActivated = savedInstanceState.getBoolean("vip")

        input.setText(inputFrom, TextView.BufferType.EDITABLE)

        input.requestFocus()
        input.setSelection(savedInstanceState.getInt("pointer"))


        output.setText(inputTo, TextView.BufferType.EDITABLE)


    }
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putInt("convertFrom", findViewById<Spinner>(R.id.input_spinner).selectedItemPosition)
        savedInstanceState.putInt("convertTo", findViewById<Spinner>(R.id.output_spinner).selectedItemPosition)

        savedInstanceState.putString("inputFrom", inputFrom)
        savedInstanceState.putString("inputTo", inputTo)

        savedInstanceState.putString("savedCopy", savedCopy)
        savedInstanceState.putBoolean("vip", vipActivated)

        savedInstanceState.putInt("opened_fragment", bottom_navigation.selectedItemId)

        savedInstanceState.putInt("pointer", input.selectionStart)

        super.onSaveInstanceState(savedInstanceState);
    }
}
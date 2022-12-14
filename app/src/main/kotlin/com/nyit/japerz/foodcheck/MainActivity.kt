package com.nyit.japerz.foodcheck

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.nyit.japerz.quickie.QRResult
import com.nyit.japerz.quickie.QRResult.QRError
import com.nyit.japerz.quickie.QRResult.QRMissingPermission
import com.nyit.japerz.quickie.QRResult.QRSuccess
import com.nyit.japerz.quickie.QRResult.QRUserCanceled
import com.nyit.japerz.quickie.ScanCustomCode
import com.nyit.japerz.quickie.ScanQRCode
import com.nyit.japerz.quickie.config.BarcodeFormat
import com.nyit.japerz.quickie.config.ScannerConfig
import com.nyit.japerz.quickie.content.QRContent
import com.nyit.japerz.foodcheck.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.View
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.handleCoroutineException


class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private var selectedBarcodeFormat = BarcodeFormat.FORMAT_ALL_FORMATS

  //QR code and customCode(BarCode) Snackbar
  private val scanQrCode = registerForActivityResult(ScanQRCode(), ::showSnackbar)
  private val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::showSnackbar)

  //Initialization of those fab buttons
  private lateinit var mAddFab: FloatingActionButton
  private lateinit var mAddBarcode: FloatingActionButton
  private lateinit var mAddQrcode: FloatingActionButton

  //Initialization of those fab texts
  private lateinit var addAlarmActionText: TextView
  private lateinit var addPersonActionText: TextView

  //This will check if the fab's are visible or not
  private var isAllFabVisible: Boolean? = null

  private lateinit var currentItem: Item

  private lateinit var docRef: DocumentReference
  private lateinit var docRef2: DocumentReference
  lateinit var tv_itemName : TextView
  lateinit var db : FirebaseFirestore
  override fun onCreate(savedInstanceState: Bundle?) {
//    tv_itemName = findViewById(R.id.idItemName)
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.fabScanQrcode.setOnClickListener {
      scanQrCode.launch(null)
    }

    binding.fabScanBarcode.setOnClickListener {
      scanCustomCode.launch(
        ScannerConfig.build {
          setBarcodeFormats(listOf(selectedBarcodeFormat)) // set interested barcode formats
          setOverlayStringRes(R.string.scan_barcode) // string resource used for the scanner overlay
          setOverlayDrawableRes(R.drawable.ic_scan_barcode) // drawable resource used for the scanner overlay
          setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
          setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
          setHorizontalFrameRatio(2.2f) // set the horizontal overlay ratio (default is 1 / square frame)
          setUseFrontCamera(false) // use the front camera
        }
      )
    }

    if (intent.extras?.getBoolean(OPEN_SCANNER) == true) scanQrCode.launch(null)

    mAddFab = findViewById(R.id.add_fab)

    mAddBarcode = findViewById(R.id.fab_scan_barcode)
    mAddQrcode = findViewById(R.id.fab_scan_qrcode)

    addAlarmActionText = findViewById(R.id.add_barcode_text)
    addPersonActionText = findViewById(R.id.add_qrcode_text)

    mAddBarcode.visibility = View.GONE
    mAddQrcode.visibility = View.GONE
    addAlarmActionText.visibility = View.GONE
    addPersonActionText.visibility = View.GONE

    isAllFabVisible = false

    mAddFab.setOnClickListener(View.OnClickListener {
      (if (!isAllFabVisible!!) {
        // when isAllFabVisible becomes true make all
        // the action name texts and FABs VISIBLE
        mAddBarcode.show()
        mAddQrcode.show()
        addAlarmActionText.visibility = View.VISIBLE
        addPersonActionText.visibility = View.VISIBLE

        // make the boolean variable true as we
        // have set the sub FABs visibility to GONE
        true
      } else {
        // when isAllFabVisible becomes true make
        // all the action name texts and FABs GONE.
        mAddBarcode.hide()
        mAddQrcode.hide()
        addAlarmActionText.visibility = View.GONE
        addPersonActionText.visibility = View.GONE

        // make the boolean variable false as we
        // have set the sub FABs visibility to GONE
        false
      }).also { isAllFabVisible = it }
    })

    //START HERE
    //Initialize Firebase
    db = Firebase.firestore
    docRef = db.collection("iteam_db").document("item1")
    currentItem = Item("","" , 0)


  }


  private fun showSnackbar(result: QRResult) {
    val text = when (result) {
      is QRSuccess -> result.content.rawValue
      QRUserCanceled -> "User canceled"
      QRMissingPermission -> "Missing permission"
      is QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
    }


    //TODO: Give this text to

    Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG).apply {
      view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)?.run {
        maxLines = 5
        setTextIsSelectable(true)
      }
      if (result is QRSuccess && result.content is QRContent.Url) {
        setAction(R.string.open_action) { openUrl(result.content.rawValue) }
      } else {
        setAction(R.string.ok_action) { }
      }

    }.show()
    if (result is QRSuccess)
    {
      if(result.content is QRContent.Url) { } //This will prevent crashes when user scanned an URL QRCode
      else
      {
        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)

        docRef2 = db.collection("iteam_db").document(result.content.rawValue)
        //docRef2 = db.collection("iteam_db").document("075720000814")
        Log.d(TAG, "X123"+result.content.rawValue)
        val source = Source.SERVER
        //This code is for testing in any barcode!
        //val test :DocumentReference = db.collection("iteam-db").document("075720000814");
        docRef2.get(source).addOnCompleteListener { task ->
          if (task.isSuccessful || result.content.rawValue.contains("https://", ignoreCase = true)) {
            // Document found in the offline cache
            val document = task.result
            currentItem.name = document.getString("name")
            currentItem.barcode = document.getString("barcode")
            currentItem.calories = document.getString("cal")?.toInt()
            if(!currentItem.name.isNullOrEmpty()) {
              tv_itemName = view.findViewById(R.id.idItemName)
              tv_itemName.text = currentItem.name
              var tv_calories = view.findViewById<TextView>(R.id.idCalNum)
              tv_calories.text = currentItem.calories.toString()
              var tv_barcode = view.findViewById<TextView>(R.id.idBarcodeNum)
              tv_barcode.text = result.content.rawValue
            }
            else{
              Log.d(TAG, "[CodeERROR]Cannot Find This Code In Database!")
              Log.d(TAG, "[CodeERROR]Code: " + result.content.rawValue)
              tv_itemName = view.findViewById(R.id.idItemName)
              tv_itemName.text = "This item is not present. Please check back later!"
              var tv_calories = view.findViewById<TextView>(R.id.idCalNum)
              tv_calories.visibility = View.GONE
              var tv_caloriesTitle = view.findViewById<TextView>(R.id.idCalTitle)
              tv_caloriesTitle.visibility = View.GONE
              var tv_barcode = view.findViewById<TextView>(R.id.idBarcodeNum)
              tv_barcode.text = result.content.rawValue
            }
          } else {
            //Exception happened
            Log.d(TAG, "[SystemError] Exception Happened: " + task.exception)
          }
        }
        val btnClose = view.findViewById<Button>(R.id.idBtnDismiss)

        btnClose.setOnClickListener{
          dialog.dismiss()
        }

        dialog.setCancelable(false)

        dialog.setContentView(view)

        dialog.show()

      }
      }

  }

  private fun openUrl(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ignored: ActivityNotFoundException) {
      // no Activity found to run the given Intent
    }
  }


  companion object {
    const val OPEN_SCANNER = "open_scanner"
  }
}
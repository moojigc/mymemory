package com.moojigbc.mymemory

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.moojigbc.mymemory.model.BoardSize
import com.moojigbc.mymemory.model.SelectedImage
import com.moojigbc.mymemory.utils.*
import java.io.ByteArrayOutputStream

class GameCreationActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_CODE = 435
        private const val READ_EXTERNAL_STORAGE_REQ_CODE = 354
        private const val READ_IMAGES_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val GAME_NAME_MIN_LEN = 3
        private const val GAME_NAME_MAX_LEN = 14

        private const val CHOOSE_IMAGES_PROMPT_TEXT = "Choose pictures"

        private const val TAG = "GameCreationActivity"
    }

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var boardSize: BoardSize
    private lateinit var btnSave: Button
    private val chosenImageUris = mutableListOf<SelectedImage>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore

    private var requiredImageCount = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_creation)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        requiredImageCount = boardSize.getPairCount()
        supportActionBar?.title = "$CHOOSE_IMAGES_PROMPT_TEXT: (${chosenImageUris.size}/$requiredImageCount)"

        // setup code for image picker
        rvImagePicker.adapter = ImagePickerAdapter(
            this,
            chosenImageUris,
            boardSize,
            object: ImagePickerAdapter.ImageClickListener {
                override fun onPlaceHolderClick() {
                    if (isPermissionGranted(this@GameCreationActivity, READ_IMAGES_PERMISSION)) {
                        launchImplicitImageSelectionIntent(allowMultiple = true)
                    } else {
                        requestPermission(this@GameCreationActivity, READ_IMAGES_PERMISSION, READ_EXTERNAL_STORAGE_REQ_CODE)
                    }
                }

                override fun onExistingImageClick(current: SelectedImage, previous: SelectedImage?) {
                    if (isPermissionGranted(this@GameCreationActivity, READ_IMAGES_PERMISSION)) {
                        launchImplicitImageSelectionIntent(allowMultiple = false)
                    } else {
                        requestPermission(this@GameCreationActivity, READ_IMAGES_PERMISSION, READ_EXTERNAL_STORAGE_REQ_CODE)
                    }
                }
            }
        )
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())

        btnSave.setOnClickListener {
            Log.i(TAG, "btnSave clicked")
            saveDataToFirebase()
        }

        etGameName.filters = arrayOf(InputFilter.LengthFilter(GAME_NAME_MAX_LEN))
        // on edit listener for save button status
        etGameName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun superFn() = super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != READ_EXTERNAL_STORAGE_REQ_CODE) {
            return superFn()
        }
        if (grantResults.isNotEmpty() && grantResults[0] == (PackageManager.PERMISSION_GRANTED)) {
            launchImplicitImageSelectionIntent()
        } else {
            Toast.makeText(
                this,
                "Game creation requires access to your photos in order to function!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
             finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PICK_IMAGE_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(
                this,
                "Couldn't load your images.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val selectedImage: Uri? = data.data
        val clipData: ClipData? = data.clipData

        // some apps return clip data, some return a single Uri
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                if (chosenImageUris.size >= requiredImageCount) {
                    if (chosenImageUris.size > requiredImageCount) {
                        Toast.makeText(
                            this,
                            "${chosenImageUris.size - requiredImageCount} extra images were not imported.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    break
                }

                val clip = clipData.getItemAt(i)
                chosenImageUris.add(SelectedImage(clip.uri, i))
            }
        } else if (selectedImage != null) {
            chosenImageUris.add(SelectedImage(selectedImage, chosenImageUris.size + 1))
        }

        rvImagePicker.adapter?.notifyDataSetChanged()

        supportActionBar?.title = "$CHOOSE_IMAGES_PROMPT_TEXT: (${chosenImageUris.size}/$requiredImageCount)"

        btnSave.isEnabled = shouldEnableSaveButton()

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun shouldEnableSaveButton(): Boolean {
        return chosenImageUris.size == requiredImageCount && etGameName.text.length >= GAME_NAME_MIN_LEN;
    }

    private fun launchImplicitImageSelectionIntent(allowMultiple: Boolean = false) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        if (allowMultiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        startActivityForResult(Intent.createChooser(intent, "Choose images"), PICK_IMAGE_CODE)
    }

    private fun getImageByteArray(photoUri: SelectedImage): ByteArray {
        // this is a ternary statement?? even though it's inside brackets???
        // you don't use return at the end?? it just implicitly returns??? WTF KOTLIN
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri.uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri.uri)
        }
        Log.i(TAG, "Og width: ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitMap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "Scaled width: ${scaledBitMap.width}, height: ${scaledBitMap.height}")

        val byteOutputStream = ByteArrayOutputStream()
        scaledBitMap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    private fun saveDataToFirebase() {
        val customGameName = etGameName.text.toString()
        Log.i(TAG, "Saving to Firebase???")
        var encounteredError = false
        val uploadedImageUrls = mutableListOf<String>()
        for ((index, photoUri) in chosenImageUris.withIndex()) {
            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "images/${customGameName}/${System.currentTimeMillis()}-${index}.jpeg"
            val photoRef = storage.reference.child(filePath)
            photoRef.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    photoRef.downloadUrl
                }.addOnCompleteListener { downloadUrlTask ->
                    if (!downloadUrlTask.isSuccessful) {
                        Log.e(TAG, "Exception with firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        encounteredError = true
                        return@addOnCompleteListener
                    }
                    if (encounteredError) {
                        return@addOnCompleteListener
                    }
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    Log.i(TAG, "finished uploading ${photoUri.uri}")
                    if (uploadedImageUrls.size == chosenImageUris.size) {
                        handleAllImagesUploaded(customGameName, uploadedImageUrls)
                    }
                }
        }
    }

    private fun handleAllImagesUploaded(gameName: String, uploadedImageUrls: MutableList<String>) {
        db.collection("games").document(gameName)
            .set(mapOf("images" to uploadedImageUrls))
            .addOnCompleteListener { gameCreationTask ->
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation", gameCreationTask.exception)
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT).show()
                }
                Log.i(TAG, "Successfully created game $gameName")
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! Let's play your game ${gameName}")
                    .setPositiveButton("OK") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }
                    .show()
            }
    }
}
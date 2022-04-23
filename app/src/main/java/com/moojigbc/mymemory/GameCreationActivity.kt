package com.moojigbc.mymemory

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moojigbc.mymemory.model.BoardSize
import com.moojigbc.mymemory.utils.EXTRA_BOARD_SIZE
import com.moojigbc.mymemory.utils.isPermissionGranted
import com.moojigbc.mymemory.utils.requestPermission

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
    private val chosenImageUris = mutableListOf<Uri>()

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
                        launchImplicitImageSelectionIntent()
                    } else {
                        requestPermission(this@GameCreationActivity, READ_IMAGES_PERMISSION, READ_EXTERNAL_STORAGE_REQ_CODE)
                    }
                }

                override fun onExistingImageClick(current: Uri, previous: Uri?) {
                    Log.i(TAG, "onExistingImageClick: current: $current, previous: $previous")
                }
            }
        )
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())

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

        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                if (chosenImageUris.size >= requiredImageCount) {
                    break
                }
                val clip = clipData.getItemAt(i)
                chosenImageUris.add(clip.uri)
            }
        } else if (selectedImage != null) {
            chosenImageUris.add(selectedImage)
        }

        rvImagePicker.adapter?.notifyDataSetChanged()

        supportActionBar?.title = "$CHOOSE_IMAGES_PROMPT_TEXT: (${chosenImageUris.size}/$requiredImageCount)"

        btnSave.isEnabled = shouldEnableSaveButton()

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun shouldEnableSaveButton(): Boolean {
        return chosenImageUris.size == requiredImageCount && etGameName.text.length >= GAME_NAME_MIN_LEN;
    }

    private fun launchImplicitImageSelectionIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        startActivityForResult(Intent.createChooser(intent, "Choose images"), PICK_IMAGE_CODE)
    }
}
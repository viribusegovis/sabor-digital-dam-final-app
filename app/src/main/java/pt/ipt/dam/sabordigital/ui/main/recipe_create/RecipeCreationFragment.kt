package pt.ipt.dam.sabordigital.ui.main.recipe_create

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.models.Category
import pt.ipt.dam.sabordigital.data.remote.models.RecipeCreate
import pt.ipt.dam.sabordigital.databinding.FragmentRecipeCreationBinding
import pt.ipt.dam.sabordigital.ui.main.MainActivity
import pt.ipt.dam.sabordigital.utils.RecipeCreationInstructionAdapter
import pt.ipt.dam.sabordigital.utils.RecipeCreationRecipeIngredientAdapter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService

class RecipeCreationFragment : Fragment() {
    private var _binding: FragmentRecipeCreationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeCreationViewModel by viewModels()
    private val ingredientAdapter = RecipeCreationRecipeIngredientAdapter()
    private val instructionAdapter = RecipeCreationInstructionAdapter()
    private val TAG = "CameraXApp"
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    // These fields are used for CameraX
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideMainFab()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.showMainFab()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()
        setupDifficultyDropdown()
        setupSubmitButton()
        setupImagePickerButton()
        setupCameraButtons()
        viewModel.loadCategories() // Load categories when view is created
    }

    private fun setupRecyclerViews() {
        binding.rvIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }

        binding.rvInstructions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = instructionAdapter
        }

        binding.btnAddIngredient.setOnClickListener {
            // Add an empty RecipeIngredient item
            ingredientAdapter.showIngredientSearch(requireContext())
        }

        binding.btnAddStep.setOnClickListener {
            instructionAdapter.addInstruction()
        }
    }


    // Permissions and launcher for CameraX
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    // Use an Activity Result Launcher to request permissions as needed.
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = REQUIRED_PERMISSIONS.all { permissions[it] == true }
            if (allGranted) {
                startCamera() // If granted, start the camera preview.
            } else {
                Toast.makeText(requireContext(), "Camera permissions denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    // Launcher for gallery activity result (if needed)
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Convert image to Base64 after selection.
                val base64Image = compressImageToBase64(requireContext(), it)
                Log.d(TAG, "Base64 string (gallery): $base64Image")
                if (base64Image != null) {
                    setImageFromBase64(binding.recipeImage, base64Image)
                }
                // Ensure image container is visible and hide the preview container.
                binding.imageContainer.visibility = View.VISIBLE
                binding.cameraPreviewContainer.visibility = View.GONE

            }
        }


    // Call this function when the "Add Image" button is clicked.
    private fun setupImagePickerButton() {
        binding.btnAddImage.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.select_image_title))
                .setItems(
                    arrayOf(
                        getString(R.string.camera),
                        getString(R.string.gallery)
                    )
                ) { dialog, which ->
                    when (which) {
                        0 -> { // Camera option
                            if (allPermissionsGranted()) {
                                showCameraPreview()
                            } else {
                                permissionLauncher.launch(REQUIRED_PERMISSIONS)
                            }
                        }

                        1 -> { // Gallery option
                            // Launch gallery picker
                            galleryLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }
    }

    private fun setupCameraButtons() {
        // "Take Photo" button in the center-bottom:
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        // "Switch Camera" button in bottom-right:
        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
        // "Back" button: return to image container.
        binding.btnBackFromCamera.setOnClickListener {
            binding.cameraPreviewContainer.visibility = View.GONE
            binding.imageContainer.visibility = View.VISIBLE
        }
    }


    // Helper to verify permissions.
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Show the camera preview: hide `imageContainer` and show `cameraPreviewContainer`, then start camera.
    private fun showCameraPreview() {
        binding.imageContainer.visibility = View.GONE
        binding.cameraPreviewContainer.visibility = View.VISIBLE
        startCamera()
    }

    // Start CameraX with a live preview.
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up Preview use case
            val preview = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation) // Respect device rotation
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            // Set up ImageCapture use case
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.previewView.display.rotation) // Match preview rotation
                .build()

            // Bind use cases to lifecycle
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    // Capture the photo using CameraX.
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    Toast.makeText(requireContext(), "Photo captured", Toast.LENGTH_SHORT).show()

                    // Ensure correct orientation using EXIF
                    savedUri?.let { uri ->
                        fixImageRotation(uri)
                        // Compress and convert the image to a Base64 string.
                        val base64Image = compressImageToBase64(requireContext(), uri)
                        Log.d(TAG, "Base64 string: $base64Image")

                        if (base64Image != null) {
                            // Now save the image for later API usage.
                            viewModel.setSelectedImage(base64Image)
                            setImageFromBase64(binding.recipeImage, base64Image)
                        }

                    }

                    // Exit camera mode: hide preview container and show image container.
                    binding.cameraPreviewContainer.visibility = View.GONE
                    binding.imageContainer.visibility = View.VISIBLE
                }
            }
        )
    }

    // Toggle between front and back camera.
    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        startCamera() // Rebind use cases with the new selector.
    }

    private fun fixImageRotation(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val bitmap =
                BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            // Save the rotated bitmap back to the same URI (optional)
            saveBitmapToUri(rotatedBitmap, uri)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun saveBitmapToUri(bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun setImageFromBase64(imageView: ImageView, base64String: String) {
        // Remove the "data:image/...;base64," prefix if present
        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }

        // Decode the Base64 string into bytes
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)

        // Convert bytes to Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        // Rotate based on EXIF data
        val rotatedBitmap = rotateImageIfRequired(bitmap, decodedBytes)

        // Set the rotated image
        imageView.setImageBitmap(rotatedBitmap)
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, byteArray: ByteArray): Bitmap {
        val inputStream = ByteArrayInputStream(byteArray)
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    // Don't forget to shut down your executor if you have one
    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        _binding = null
    }

    private fun setupDifficultyDropdown() {
        val difficulties = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_hard)
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            difficulties
        )
        binding.actvDifficulty.inputType = InputType.TYPE_NULL
        binding.actvDifficulty.setAdapter(adapter)
    }


    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmit.isEnabled = !isLoading
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.chipGroupCategories.removeAllViews()
            categories.forEach { category ->
                val chip = Chip(context).apply {
                    text = category.name
                    isCheckable = true
                    tag = category
                }
                binding.chipGroupCategories.addView(chip)
            }
        }

        viewModel.selectedImage.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.btnAddImage.hide()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                val recipe = createRecipeFromForm()
                viewModel.createRecipe(requireContext(), recipe)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate title
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.tilTitle.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        val selectedCategories = binding.chipGroupCategories.checkedChipIds.count()
        // Validate categories
        if (selectedCategories == 0) {
            binding.categoryError.apply {
                text = getString(R.string.error_required_field)
                visibility = View.VISIBLE
            }
            isValid = false
        } else {
            binding.categoryError.visibility = View.GONE
        }

        // Validate description
        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        // Validate prep time
        if (binding.etPrepTime.text.isNullOrBlank()) {
            binding.tilPrepTime.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilPrepTime.error = null
        }

        // Validate servings
        if (binding.etServings.text.isNullOrBlank()) {
            binding.tilServings.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilServings.error = null
        }

        // Validate difficulty
        if (binding.actvDifficulty.text.isNullOrBlank()) {
            binding.tilDifficulty.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilDifficulty.error = null
        }

        // Validate ingredients
        if (!ingredientAdapter.validateAllIngredients()) {
            binding.ingredientsError.apply {
                text = getString(R.string.error_required_field)
                visibility = View.VISIBLE
            }
            isValid = false
        } else {
            binding.ingredientsError.visibility = View.GONE
        }

        // Validate instructions
        if (!instructionAdapter.validateAllInstructions()) {
            binding.instructionsError.apply {
                text = getString(R.string.error_required_field)
                visibility = View.VISIBLE
            }
            isValid = false
        } else {
            binding.instructionsError.visibility = View.GONE
        }



        return isValid
    }

    fun compressImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            // Load bitmap from the URI.
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress the bitmap to JPEG at 70% quality.
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            // Convert byte array to Base64 string.
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun getCurrentUser(): Int {
        val sharedPrefs = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val user_id = sharedPrefs.getInt("user_id", 0)
        return user_id
    }

    private fun createRecipeFromForm(): RecipeCreate {

        val categories = binding.chipGroupCategories.checkedChipIds.map { chipId ->
            binding.chipGroupCategories.findViewById<Chip>(chipId).tag as Category
        }

        return RecipeCreate(
            author_id = getCurrentUser(),
            title = binding.etTitle.text.toString(),
            description = binding.etDescription.text.toString(),
            preparation_time = binding.etPrepTime.text.toString().toIntOrNull() ?: 0,
            servings = binding.etServings.text.toString().toIntOrNull() ?: 0,
            difficulty = mapDifficulty(binding.actvDifficulty.text.toString()),
            ingredients = ingredientAdapter.getIngredients(),
            instructions = instructionAdapter.getInstructions(),
            categories = categories,
            image_url = viewModel.selectedImage.value
        )
    }

    private fun mapDifficulty(input: String): String {
        return when (input.trim().uppercase(Locale("PT-PT"))) {
            "FÁCIL", "FACIL" -> "FACIL"
            "MEDIO", "MÉDIO" -> "MEDIO"
            "DIFÍCIL", "DIFIL", "DÍFICIL" -> "DIFICIL"
            else -> input.uppercase(Locale("PT-PT"))
        }
    }

}

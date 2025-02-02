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

/**
 * Fragment that handles the creation of a new recipe.
 *
 * It covers form input validation, image selection (via camera or gallery),
 * capturing and processing images with CameraX, and building a RecipeCreate object.
 */
class RecipeCreationFragment : Fragment() {

    private var _binding: FragmentRecipeCreationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeCreationViewModel by viewModels()
    private val ingredientAdapter = RecipeCreationRecipeIngredientAdapter()
    private val instructionAdapter = RecipeCreationInstructionAdapter()
    private val TAG = "CameraXApp"
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    // Fields used for CameraX image capture.
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Hide the main floating action button when this fragment is active.
        (activity as? MainActivity)?.hideMainFab()
    }

    override fun onPause() {
        super.onPause()
        // Restore the FAB on pause.
        (activity as? MainActivity)?.showMainFab()
    }

    /**
     * Inflates the fragment view using view binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeCreationBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up UI components and observers, and loads categories for recipe creation.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()
        setupDifficultyDropdown()
        setupSubmitButton()
        setupImagePickerButton()
        setupCameraButtons()
        viewModel.loadCategories() // Load available categories at view creation.
    }

    /**
     * Configures the RecyclerViews for ingredient and instruction lists.
     * Also sets up button click listeners to add new ingredients or instruction steps.
     */
    private fun setupRecyclerViews() {
        binding.rvIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }

        binding.rvInstructions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = instructionAdapter
        }

        // Trigger ingredient search dialog.
        binding.btnAddIngredient.setOnClickListener {
            ingredientAdapter.showIngredientSearch(requireContext())
        }

        // Add an empty instruction step.
        binding.btnAddStep.setOnClickListener {
            instructionAdapter.addInstruction()
        }
    }

    ////////////// Permission Handling and Activity Result Launchers //////////////

    // List of permissions required for CameraX functionality.
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    /**
     * ActivityResultLauncher to request camera (and storage) permissions.
     * If granted, starts the camera preview.
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = REQUIRED_PERMISSIONS.all { permissions[it] == true }
            if (allGranted) {
                startCamera() // Start camera preview if all permissions are granted.
            } else {
                Toast.makeText(requireContext(), "Camera permissions denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    /**
     * Launcher for selecting an image from the gallery.
     * Converts the selected image to a Base64 string and updates the UI accordingly.
     */
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Convert image to Base64 and log it.
                val base64Image = compressImageToBase64(requireContext(), it)
                Log.d(TAG, "Base64 string (gallery): $base64Image")
                if (base64Image != null) {
                    setImageFromBase64(binding.recipeImage, base64Image)
                }
                // Display image container and hide the camera preview.
                binding.imageContainer.visibility = View.VISIBLE
                binding.cameraPreviewContainer.visibility = View.GONE
            }
        }

    /**
     * Sets up a button to allow the user to pick an image from the camera or gallery.
     */
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
                            galleryLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }
    }

    /**
     * Configures camera-related buttons:
     * - Capture photo.
     * - Switch between front and back cameras.
     * - Exit camera preview mode.
     */
    private fun setupCameraButtons() {
        // Button to take a photo.
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
        // Button to switch camera.
        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
        // Button to return to the image container from camera preview.
        binding.btnBackFromCamera.setOnClickListener {
            binding.cameraPreviewContainer.visibility = View.GONE
            binding.imageContainer.visibility = View.VISIBLE
        }
    }

    /**
     * Checks whether all required permissions (camera, write storage) are granted.
     *
     * @return true if all permissions are granted, false otherwise.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Shows the camera preview by hiding the image container and starting CameraX.
     */
    private fun showCameraPreview() {
        binding.imageContainer.visibility = View.GONE
        binding.cameraPreviewContainer.visibility = View.VISIBLE
        startCamera()
    }

    /**
     * Initializes and starts CameraX with both Preview and ImageCapture use cases.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the Preview use case.
            val preview = Preview.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

            // Set up the ImageCapture use case.
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.previewView.display.rotation)
                .build()

            try {
                // Unbind any previously bound use cases.
                cameraProvider.unbindAll()
                // Bind the use cases to the lifecycle.
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

    /**
     * Captures a photo using CameraX.
     *
     * The captured image is saved via MediaStore, its orientation is fixed using EXIF,
     * and it is converted to a Base64 string for API usage.
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Generate a unique file name.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
            }
        }

        // Configure the output options.
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // Take the picture and handle the result.
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

                    // Correct the image orientation.
                    savedUri?.let { uri ->
                        fixImageRotation(uri)
                        // Compress and convert the image to a Base64 string.
                        val base64Image = compressImageToBase64(requireContext(), uri)
                        Log.d(TAG, "Base64 string: $base64Image")

                        if (base64Image != null) {
                            viewModel.setSelectedImage(base64Image)
                            setImageFromBase64(binding.recipeImage, base64Image)
                        }
                    }

                    // Exit camera mode.
                    binding.cameraPreviewContainer.visibility = View.GONE
                    binding.imageContainer.visibility = View.VISIBLE
                }
            }
        )
    }

    /**
     * Toggles between the front and back cameras and restarts the camera preview.
     */
    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        startCamera() // Rebind use cases with the new camera selector.
    }

    /**
     * Fixes the image rotation based on EXIF data.
     *
     * @param uri The URI of the captured image.
     */
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

            // Optionally, save the rotated image back.
            saveBitmapToUri(rotatedBitmap, uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves a given bitmap to the provided URI.
     *
     * @param bitmap The bitmap to save.
     * @param uri The destination URI.
     */
    private fun saveBitmapToUri(bitmap: Bitmap, uri: Uri) {
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            outputStream?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Converts a Base64 string to a bitmap and sets it in an ImageView.
     *
     * @param imageView The ImageView to set the image.
     * @param base64String The Base64-encoded image.
     */
    fun setImageFromBase64(imageView: ImageView, base64String: String) {
        // Remove any Base64 header.
        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }

        // Decode the string and create a Bitmap.
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        val rotatedBitmap = rotateImageIfRequired(bitmap, decodedBytes)
        imageView.setImageBitmap(rotatedBitmap)
    }

    /**
     * Checks the EXIF data of the byte array and rotates the bitmap if needed.
     *
     * @param bitmap The bitmap to rotate.
     * @param byteArray The original byte array containing the image data.
     * @return The rotated (or original) bitmap.
     */
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

    /**
     * Rotates a given bitmap by the specified number of degrees.
     *
     * @param bitmap The bitmap to rotate.
     * @param degrees The rotation angle in degrees.
     * @return The rotated bitmap.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Shuts down the camera executor and cleans up view binding upon view destruction.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
        _binding = null
    }

    /**
     * Sets up the difficulty dropdown (spinner) with localized difficulty options.
     */
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

    /**
     * Sets up observers for LiveData objects from the ViewModel.
     * Handles loading state, categories display, and image selection.
     */
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

    /**
     * Sets up the submit button to validate the form and create a recipe.
     */
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                val recipe = createRecipeFromForm()
                viewModel.createRecipe(requireContext(), recipe)
            }
        }
    }

    /**
     * Validates all required form fields for recipe creation.
     *
     * @return true if every field is valid; false otherwise.
     */
    private fun validateForm(): Boolean {
        var isValid = true

        // Validate the title field.
        if (binding.etTitle.text.isNullOrBlank()) {
            binding.tilTitle.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        // Validate that at least one category is selected.
        val selectedCategories = binding.chipGroupCategories.checkedChipIds.count()
        if (selectedCategories == 0) {
            binding.categoryError.apply {
                text = getString(R.string.error_required_field)
                visibility = View.VISIBLE
            }
            isValid = false
        } else {
            binding.categoryError.visibility = View.GONE
        }

        // Validate description.
        if (binding.etDescription.text.isNullOrBlank()) {
            binding.tilDescription.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilDescription.error = null
        }

        // Validate preparation time.
        if (binding.etPrepTime.text.isNullOrBlank()) {
            binding.tilPrepTime.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilPrepTime.error = null
        }

        // Validate servings.
        if (binding.etServings.text.isNullOrBlank()) {
            binding.tilServings.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilServings.error = null
        }

        // Validate difficulty.
        if (binding.actvDifficulty.text.isNullOrBlank()) {
            binding.tilDifficulty.error = getString(R.string.error_required_field)
            isValid = false
        } else {
            binding.tilDifficulty.error = null
        }

        // Validate that ingredients have been added.
        if (!ingredientAdapter.validateAllIngredients()) {
            binding.ingredientsError.apply {
                text = getString(R.string.error_required_field)
                visibility = View.VISIBLE
            }
            isValid = false
        } else {
            binding.ingredientsError.visibility = View.GONE
        }

        // Validate that instructions have been added.
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

    /**
     * Compresses an image from a URI to a Base64 encoded JPEG string.
     *
     * @param context The context to access the content resolver.
     * @param imageUri The URI of the image.
     * @return A Base64 string representation of the compressed image, or null if an error occurs.
     */
    fun compressImageToBase64(context: Context, imageUri: Uri): String? {
        return try {
            // Load the bitmap from the provided URI.
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress the bitmap into JPEG with 70% quality.
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            // Encode the byte array to a Base64 string.
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves the current user ID from SharedPreferences.
     *
     * @return The user ID (default is 0 if not found).
     */
    private fun getCurrentUser(): Int {
        val sharedPrefs = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("user_id", 0)
    }

    /**
     * Creates a RecipeCreate object from user input in the form.
     *
     * @return A populated RecipeCreate instance.
     */
    private fun createRecipeFromForm(): RecipeCreate {

        // Retrieve selected categories from the chip group.
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

    /**
     * Maps the user input from the difficulty dropdown to a standardized value.
     *
     * @param input The difficulty string as input by the user.
     * @return The mapped difficulty string (e.g., FACIL, MEDIO, DIFICIL).
     */
    private fun mapDifficulty(input: String): String {
        return when (input.trim().uppercase(Locale("PT-PT"))) {
            "FÁCIL", "FACIL" -> "FACIL"
            "MEDIO", "MÉDIO" -> "MEDIO"
            "DIFÍCIL", "DIFIL", "DÍFICIL" -> "DIFICIL"
            else -> input.uppercase(Locale("PT-PT"))
        }
    }
}
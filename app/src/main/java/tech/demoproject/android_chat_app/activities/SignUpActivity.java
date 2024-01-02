package tech.demoproject.android_chat_app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import tech.demoproject.android_chat_app.databinding.ActivitySignUpBinding;
import tech.demoproject.android_chat_app.utilities.Constants;
import tech.demoproject.android_chat_app.utilities.EncryptionUtils;
import tech.demoproject.android_chat_app.utilities.PasswordHasher;
import tech.demoproject.android_chat_app.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {

    // ActivitySignUpBinding class is automatically generated from our layout file: 'activity_sign_up'
    private ActivitySignUpBinding binding;

    private PreferenceManager preferenceManager;
    private String publicKey;
    private String privateKey;
    private String encodeImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set binding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        // set listener for all action
        setListeners();
    }

    private void setListeners(){
        // you can see: an instance of a binding class contains direct references to all view that have an ID in the corresponding layout
        binding.textSignIn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignInActivity.class)));

        // set onClick for button Sign Up
        binding.buttonSignUp.setOnClickListener(v -> {
            // check if all information is entered correctly
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        //set onClick for layout add image profile
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        generateUserKey();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        //put hashed password
        user.put(
                Constants.KEY_PASSWORD,
                hashPassword(binding.inputPassword.getText().toString())
        );
        user.put(Constants.KEY_IMAGE,encodeImage);
        // put user key to database
        user.put(Constants.KEY_USER_PUBLIC_KEY,publicKey.toString());
        user.put(Constants.KEY_USER_PRIVATE_KEY,privateKey.toString());
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                //when sign upp an account success
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodeImage);
                    // user key
                    preferenceManager.putString(Constants.KEY_USER_PUBLIC_KEY,publicKey.toString());
                    preferenceManager.putString(Constants.KEY_USER_PRIVATE_KEY,privateKey.toString());

                    // Access to MainActivity
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                //when sign upp an account failure
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private void generateUserKey() {
        EncryptionUtils encryptionUtils = new EncryptionUtils();
        try {
            KeyPair userKey = encryptionUtils.generateKeyPair();
            publicKey = userKey.getPublic().toString();
            privateKey = userKey.getPrivate().toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String hashPassword(String password) {
        PasswordHasher hash = new PasswordHasher();
        try {
            return hash.hashPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // this function will encode an image you choose in gallery.
    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Pick image function
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    // Check valid sign up
    private Boolean isValidSignUpDetails(){
        if(encodeImage == null){
            showToast("Select your image");
            return false;
        }else if(binding.inputName.getText().toString().trim().isEmpty()){
            showToast("Enter name");
            return false;
        }else if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast("Confirm password");
            return false;
        }else if(!binding.inputPassword.getText().toString()
                .equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Password and Confirm password must be same");
            return false;
        }
        // if all information was valid and correct request.
        return true;
    }

    // loading for Sign Up
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}

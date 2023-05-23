package com.example.crudpractice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
     StorageReference storageReference;

    EditText editname,editemail,editage,editphone,editsex,editaddress,editusername,editpassword;

    ImageView imageView;
    Button button;
    Uri imageUri;

    // Request code for selecting an image from the gallery
    private static final int PICK_IMAGE_REQUEST = 1000;
    private static final int RESULT_OK = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //referencing the ids

        editname= findViewById(R.id.name);
        editemail=findViewById(R.id.email);
        editage=findViewById(R.id.age);
        editphone=findViewById(R.id.phone);
        editsex=findViewById(R.id.sex);
        editaddress=findViewById(R.id.address);
        editusername=findViewById(R.id.username);
        editpassword=findViewById(R.id.password);
        button=findViewById(R.id.button);
        // Reference to the ImageView with id "img"
        imageView = findViewById(R.id.img);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri != null) {
                    uploadImage();
                } else {
                    Toast.makeText(MainActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
 }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

        }
    }

    private void uploadImage() {
        // ...
        databaseReference=FirebaseDatabase.getInstance().getReference().child("profile");
        storageReference=FirebaseStorage.getInstance().getReference();
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

        fileReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrlTask = taskSnapshot.getStorage().getDownloadUrl();
                        downloadUrlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String name = editname.getText().toString().trim();
                                String email = editemail.getText().toString().trim();
                                String age = editage.getText().toString().trim();
                                String phone = editphone.getText().toString().trim();
                                String sex = editsex.getText().toString().trim();
                                String address = editaddress.getText().toString().trim();
                                String username = editusername.getText().toString().trim();
                                String password = editpassword.getText().toString().trim();
                                String downloadUrl = uri.toString();
                                String uploadId = databaseReference.push().getKey();

                                if (TextUtils.isEmpty(name)) {
                                    editname.setError("Please add your name");
                                    editname.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(email)) {
                                    editemail.setError("Provide your email");
                                    editemail.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(phone)) {
                                    editphone.setError("Provide your phone number");
                                    editphone.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(sex)) {
                                    editsex.setError("Are you a male or female");
                                    editsex.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(address)) {
                                    editaddress.setError("Provide your address");
                                    editaddress.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(username)) {
                                    editusername.setError("Provide your username");
                                    editusername.requestFocus();
                                    return;
                                }
                                if (TextUtils.isEmpty(password)) {
                                    editpassword.setError("Provide your password");
                                    editpassword.requestFocus();
                                    return;
                                }

                                Profile prof = new Profile(uploadId, name, email, downloadUrl, age, phone, sex, address, username, password);
                                databaseReference.child(uploadId).setValue(prof)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Clear the EditText fields
                                                editname.setText("");
                                                editemail.setText("");
                                                editage.setText("");
                                                editphone.setText("");
                                                editsex.setText("");
                                                editaddress.setText("");
                                                editusername.setText("");
                                                editpassword.setText("");
                                                imageView.setImageResource(R.drawable.imageupload);
                                                Toast.makeText(MainActivity.this, "Profile recorded successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(MainActivity.this, Retrieve.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
package com.example.crudpractice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Retrieve extends AppCompatActivity {
    EditText textname, textemail, txtage, txtphone, txtaddress, txtusername, txtsex, txtpassword;
    Button retrieve, update, delete;
    ImageView imageView;
    Uri imageUri;
    // Request code for selecting an image from the gallery
    private static final int PICK_IMAGE_REQUEST = 1000;
    private static final int RESULT_OK = -1;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("profile");
    StorageReference storageReference= FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve);
        // referencing the ids

        retrieve = findViewById(R.id.data);
        update = findViewById(R.id.names);
        delete = findViewById(R.id.delete);
        retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textname = findViewById(R.id.name);
                textemail = findViewById(R.id.email);
                txtage = findViewById(R.id.age);
                txtphone = findViewById(R.id.phone);
                txtaddress = findViewById(R.id.address);
                txtusername = findViewById(R.id.username);
                txtsex = findViewById(R.id.sex);
                txtpassword = findViewById(R.id.password);
                // Create a reference to the Firebase database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("profile");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            String names = snapshot1.child("name").getValue(String.class);
                            String email = snapshot1.child("email").getValue(String.class);
                            String age = snapshot1.child("age").getValue(String.class);
                            String phone = snapshot1.child("phone").getValue(String.class);
                            String sex = snapshot1.child("sex").getValue(String.class);
                            String address = snapshot1.child("address").getValue(String.class);
                            String username = snapshot1.child("username").getValue(String.class);
                            String password = snapshot1.child("password").getValue(String.class);
                            String downloadUrl = snapshot1.child("imageUri").getValue(String.class);
                            textname.setText(names);
                            textemail.setText(email);
                            txtage.setText(age);
                            txtphone.setText(phone);
                            txtsex.setText(sex);
                            txtaddress.setText(address);
                            txtusername.setText(username);
                            txtpassword.setText(password);
                            imageView = findViewById(R.id.img);
                            Picasso.get().load(downloadUrl).into(imageView);
                            Toast.makeText(Retrieve.this, "Congs data retrieved", Toast.LENGTH_SHORT).show();
//
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {


                    }
                });


            }
        });



        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

    }




    private void updateProfile() {
        // Get the profile ID you want to update
        String uploadId = "-NVseb6H0bUUD9HohkjY"; // Replace with the actual profile ID

        // Create a DatabaseReference for the specific profile
        DatabaseReference profileRef = reference.child(uploadId);

        // Retrieve the existing profile data
        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                if (profile != null) {
                    // Update the profile data
                    String name = textname.getText().toString().trim();
                    String email = textemail.getText().toString().trim();
                    String age = txtage.getText().toString().trim();
                    String phone = txtphone.getText().toString().trim();
                    String sex = txtsex.getText().toString().trim();
                    String address = txtaddress.getText().toString().trim();
                    String username = txtusername.getText().toString().trim();
                    String password = txtpassword.getText().toString().trim();

                    // Update the profile fields
                    profile.setName(name);
                    profile.setEmail(email);
                    profile.setAge(age);
                    profile.setPhone(phone);
                    profile.setSex(sex);
                    profile.setAddress(address);
                    profile.setUsername(username);
                    profile.setPassword(password);

                    // Upload the new image to Firebase Storage
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openFileChooser();

                        }
                    });
                    if(imageUri==null){
                        Toast.makeText(Retrieve.this, "Image not selected", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Retrieve.this, "image selected", Toast.LENGTH_SHORT).show();
                    }
                    if (imageUri != null) {

                        StorageReference fileReference = storageReference.child(uploadId + "." + getFileExtension(imageUri));
                        fileReference.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Get the download URL of the new image
                                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String downloadUrl = uri.toString();
                                                // Update the profile image URL
                                                profile.setImageUri(downloadUrl);

                                                // Update the profile in Firebase Realtime Database
                                                profileRef.setValue(profile)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(Retrieve.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(Retrieve.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Retrieve.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If no new image was selected, update the profile data only
                        profileRef.setValue(profile)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(Retrieve.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Retrieve.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occurred during the retrieval
                Toast.makeText(Retrieve.this, "Error" + databaseError.getMessage(), Toast.LENGTH_SHORT);

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
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


}

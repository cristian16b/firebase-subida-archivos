package com.example.firebase_subida_archivos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // views for button
    private Button btnSelect, btnUpload;

    // view for image view
    private ImageView imageView;

    // textview detalles
    private TextView detallesText;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    //lista de archivos subidos
    private ListView lv1;

    private String[] archivos = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            Intent i = new Intent(this, LoginActivity.class );
            startActivity(i);
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Bienvenido " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            // Load chat room contents
//            mostrarArchivos();
        }


        // initialise views
        btnSelect = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
//        imageView = findViewById(R.id.imgView);
        detallesText = findViewById(R.id.detallesArchivo);


        // on pressing btnSelect SelectImage() is called
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImage();
            }
        });

        // on pressing btnUpload uploadFile() is called
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                Toast
//                        .makeText(MainActivity.this,
//                                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
//                                Toast.LENGTH_LONG)
//                        .show();
                uploadFile();
            }
        });

//        lv1=findViewById(R.id.listaArchivos);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, archivos);
//        lv1.setAdapter(adapter);

        donwloadFile("12225");
    }


    protected void mostrarArchivos() {
        //Log.i("directorio",FirebaseAuth.getInstance().getCurrentUser().getEmail()+"/");
//        Log.i("directorio",storage.toString());

//       Si recorro los archivos que subio el usuario xxxxx@correo.com uso
        StorageReference listRef = storage.getReference().child(FirebaseAuth.getInstance().getCurrentUser().getEmail()+"/");

//        Si quiero recorrer los directorios en el indice del storage uso:
//        StorageReference listRef = storage.getReference().child("/");

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        Log.i("ingreso","uff magia algo paso");

//                       Primer bucle recorre directorios
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                            Log.i("ingreso","bucle 1 vuelta " + prefix.getName());
                        }

//                        segundo bucle recorre los archivos
                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            String i = item.getName();
                            Log.i("ingreso","bucle 2 vuelta"+i + item.getPath());

//                            donwloadFile(i);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                                        Toast
                        .makeText(MainActivity.this,
                                "Ocurrio un error al intentar recuperar los archivos, cierre session a intente ingresar de nuevo.",
                                Toast.LENGTH_LONG)
                        .show();
                    }
                });
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class );

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        // Sign Out Google...
    }

    // Select Image method
    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Seleccionar archivo..."),
                PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();

            File file = new File(filePath.getPath());
            String detalle = "Archivo seleccionado: " + file.getName();

            detallesText.setText(detalle);
//            try {

                // Setting image on image view using Bitmap
//                Bitmap bitmap = MediaStore
//                        .Images
//                        .Media
//                        .getBitmap(
//                                getContentResolver(),
//                                filePath);
//                imageView.setImageBitmap(bitmap);

//            }
//
//            catch (IOException e) {
//                // Log the exception
//                e.printStackTrace();
//            }
        }
    }

    // UploadImage method
    private void uploadFile()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Subiendo...");
            progressDialog.show();

            File file = new File(filePath.getPath());

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            FirebaseAuth.getInstance().getCurrentUser().getEmail()
                                    +
                            "/"
                                    + file.getName());

            detallesText.setText("");

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(MainActivity.this,
                                                    "Archivo subido!!",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Ocurrio un error: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }

    private void donwloadFile(String nombre){
        String path = FirebaseAuth.getInstance().getCurrentUser().getEmail()+"/"+nombre;
        Log.i("ruta",path);
        StorageReference fileRef = storageReference.child(path);


        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Bajando...");
        progressDialog.show();

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Data for "images/island.jpg" is returns, use this as needed
                progressDialog.dismiss();
                Toast
                        .makeText(MainActivity.this,
                                "Archivo descargado!!",
                                Toast.LENGTH_LONG)
                        .show();
                Log.i("archivo url",uri.toString());
                detallesText.setText(uri.toString());
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                progressDialog.dismiss();
                Toast
                        .makeText(MainActivity.this,
                                "Ocurrio un error: " + exception.getMessage(),
                                Toast.LENGTH_LONG)
                        .show();
            }
        });

    }
}
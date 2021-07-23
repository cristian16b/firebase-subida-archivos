package com.example.firebase_subida_archivos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class SubirArchivoActivity extends AppCompatActivity {

    // views for button
    private FloatingActionButton btnSelect, btnUpload;

    // view for image view
    private ImageView imageView;

    // textview detalles
    private TextView detallesText;

    // Uri indicates, where the image will be picked from
    private Uri filePath;
    private String displayName;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    EditText editTextComentario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_archivo);

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnSelect = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        detallesText = findViewById(R.id.detallesArchivo);
        editTextComentario = findViewById(R.id.editTextComentario);

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
//                Log.i("mensaje",input.getText().toString());

                if(editTextComentario.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(),
                            "Debe ingresar una descripción del archivo.",
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    uploadFile();
                }
            }
        });
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

            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            displayName = null;

            // obtengo el nombre del archivo
            obtenerNombreArchivo(uri,uriString,myFile);

            //  obtengo el tamaño
//            Log.i("peso",esMayor10megas(uri,uriString,myFile) + "");

            if(esMayor10megas(uri,uriString,myFile)) {
                filePath = null; displayName = null;
                Toast
                        .makeText(SubirArchivoActivity.this,
                                "Su archivo no puede ser subido porque pesa mas de 10 MegaBytes.",
                                Toast.LENGTH_LONG)
                        .show();
            } else {
                String detalle = "Archivo seleccionado: " + displayName;
                //muestro el archivo seleccionado
                detallesText.setText(detalle);
            }
        }
    }

    private boolean esMayor10megas(Uri uri,String uriString,File myFile) {
        boolean aux = false;
        Cursor cursor = null;
        String size = "0";
        try {
            cursor = this.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
            }

            //transform in MB
            Long sizeInMb = Long.valueOf(size) / (1024 * 1024);
//            Log.i("peso",sizeInMb.toString());
            if(sizeInMb>10){
                aux = true;
            }

        } finally {
            cursor.close();
        }

        return aux;
    }


    private void obtenerNombreArchivo(Uri uri,String uriString,File myFile) {
        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
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
                                    + displayName);

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

                                    //guardamos en realtime la info
//                                    copiado del otro proyecto
//                                    FirebaseDatabase.getInstance().getReference().push()
//                                            .setValue(new ChatMessage(input.getText().toString(),
//                                                    FirebaseAuth.getInstance()
//                                                            .getCurrentUser()
//                                                            .getDisplayName())
//                                            );



                                    String usuarioId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                                    JSONObject json = new JSONObject();
//                                    JSONObject item = new JSONObject();
//                                    try {
//                                        item.put("information", "test");
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                    try {
//                                        json.put("name", "student");
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                    FirebaseDatabase.getInstance().getReference()
//                                            .push()
//                                            .setValue(usuarioId);

                                    Task<Uri> downloadUri = ref.getDownloadUrl();

                                    String usuarioEmail = FirebaseAuth.getInstance()
                                            .getCurrentUser()
                                            .getEmail();

                                    miFile file = new miFile(
                                            displayName,
                                            editTextComentario.getText().toString(),
                                            usuarioEmail,
                                            downloadUri.toString()
                                    );
                                    Log.i("nombre",editTextComentario.getText().toString());
                                    Log.i("mifile",file.toString());

                                    FirebaseDatabase.getInstance().getReference().child(usuarioId).push()
                                            .setValue(file);




                                    editTextComentario.setText("");

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(SubirArchivoActivity.this,
                                                    "Archivo subido!!",
                                                    Toast.LENGTH_LONG)
                                            .show();

//                                    finish();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(SubirArchivoActivity.this,
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
        } else {
            Toast
                    .makeText(SubirArchivoActivity.this,
                            "Seleccione un archivo",
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void volver(View view) {
        finish();
    }
}
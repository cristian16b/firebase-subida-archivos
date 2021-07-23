package com.example.firebase_subida_archivos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.example.firebase_subida_archivos.miFile;

import static android.R.layout.simple_list_item_1;

public class MainActivity extends AppCompatActivity {


    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    //lista de archivos subidos
    private ListView lv1;

//    private ArrayList lista;

    ArrayAdapter<String> arrayAdapter;

    private ArrayList<miFile> lista = new ArrayList<>();

    private AtomPayListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

//        lv1=findViewById(R.id.listaArchivos);
//        lista = new ArrayList<>();
//        arrayAdapter = new ArrayAdapter<String>(this, simple_list_item_1, lista);
//        lv1.setAdapter(arrayAdapter);

        // initialise views

//        imageView = findViewById(R.id.imgView);



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
            mostrarArchivos();
        }



//        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
//                donwloadFile(lv1.getItemAtPosition(i).toString());
//                Log.i("seleccionado=",lv1.getItemAtPosition(i).toString());
//            }
//        });
    }

    public void abrirCargaArchivos(View view) {
        Intent i = new Intent(this, SubirArchivoActivity.class );
        startActivity(i);
    }


    public void descargarArchivo(View view) {
        miFile item = (miFile)view.getTag();
        try {

            String nombre = item.getNombreFile();
//            Log.i("archivo",nombre);
            donwloadFile(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Ocurrio un error. El registro seleccionado no se encontro o no se registro correctamente el nombre del recurso.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }


    protected void mostrarArchivos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.i("usuario",user.getEmail());


        DatabaseReference db =
                FirebaseDatabase.getInstance().getReference().child(user.getUid());

        ListView atomPaysListView = (ListView)findViewById(R.id.listaArchivos);
        adapter = new AtomPayListAdapter(MainActivity.this, lista);


        atomPaysListView.setAdapter(adapter);

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                  miFile mensaje = dataSnapshot.getValue(miFile.class);
                  lista.add(mensaje);
                  Collections.reverse(lista);
                  adapter.notifyDataSetChanged();

//                  atomPaysListView.setSelection(atomPaysListView.getAdapter().getCount()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAGLOG","onChildChanged: {" + dataSnapshot.getKey() + ": " + dataSnapshot.getValue() + "}");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("TAGLOG", "onChildRemoved: {" + dataSnapshot.getKey() + ": " + dataSnapshot.getValue() + "}");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAGLOG", "onChildMoved: " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAGLOG","Error!", databaseError.toException());
            }
        };

        db.addChildEventListener(childEventListener);
//        if(!lista.isEmpty()) {
//            lista.clear();
//        }
////
//
////       Si recorro los archivos que subio el usuario xxxxx@correo.com uso
//        StorageReference listRef = storage.getReference().child(FirebaseAuth.getInstance().getCurrentUser().getEmail()+"/");
////
//////        Si quiero recorrer los directorios en el indice del storage uso:
////        StorageReference listRef = storage.getReference().child("/");
////
//        listRef.listAll()
//                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
//                    @Override
//                    public void onSuccess(ListResult listResult) {
//
//                        Log.i("ingreso","uff magia algo paso");
//
////                       Primer bucle recorre directorios
//                        for (StorageReference prefix : listResult.getPrefixes()) {
//                            // All the prefixes under listRef.
//                            // You may call listAll() recursively on them.
//                            Log.i("ingreso","bucle 1 vuelta " + prefix.getName());
//                        }
//
////                        segundo bucle recorre los archivos
//                        for (StorageReference item : listResult.getItems()) {
//                            // All the items under listRef.
//                            String i = item.getName();
//                            Log.i("ingreso","bucle 2 vuelta"+i + item.getName());
//
////                            donwloadFile(i);
//
//                            lista.add(item.getName());
////                            arrayAdapter.notifyDataSetChanged();
////                            Log.i("vector",lista.toString());
//                        }
//
//                        if(!lista.isEmpty()) {
//                            arrayAdapter.notifyDataSetChanged();
//                       }
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Uh-oh, an error occurred!
//                                        Toast
//                        .makeText(MainActivity.this,
//                                "Ocurrio un error al intentar recuperar los archivos, cierre session a intente ingresar de nuevo.",
//                                Toast.LENGTH_LONG)
//                        .show();
//                    }
//                });
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class );

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        // Sign Out Google...
    }


    private void donwloadFile(String nombre){
        String path = FirebaseAuth.getInstance().getCurrentUser().getEmail()+"/"+nombre;
        Log.i("ruta",path);
        StorageReference fileRef = storageReference.child(path);
//
//
//        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Bajando...");
        progressDialog.show();

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Data for "images/island.jpg" is returns, use this as needed

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

                progressDialog.dismiss();
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
//
    }
}
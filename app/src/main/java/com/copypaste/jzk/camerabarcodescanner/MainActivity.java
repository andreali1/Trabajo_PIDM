package com.copypaste.jzk.camerabarcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private Button btnScanner;
    private TextView tvBarCode;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("message");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScanner = findViewById(R.id.btnScanner);
        //Texto del Resultado
        tvBarCode = findViewById(R.id.tvBarCode);
        //Variable Escanear

        btnScanner.setOnClickListener(mOnClickListener);


        databaseReference= FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
            if (result.getContents() != null)
            {
                tvBarCode.setText("El código de barras es:\n" + result.getContents());
            }else {
                tvBarCode.setText("Error al escanear el código de barras");
            }
    }


    //Boton Escanear

    private View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String codigo= tvBarCode.getText().toString();
            switch (v.getId()){
                case R.id.btnScanner:
                    new IntentIntegrator(MainActivity.this).initiateScan();
                    break;
            }

            cargarDatos(codigo);
            //finish();
        }
    };

    private void cargarDatos(final String result)
    {
        Map<String, Object> datosUsuario = new HashMap<>();

        datosUsuario.put("Codigo", result);
        //Toast.makeText(getApplicationContext(), "Registro satisfactorio", Toast.LENGTH_SHORT).show();

        if (result.equals("")) {
            Toast.makeText(getApplicationContext(), "No se pudo Escanear", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Registro satisfactorio", Toast.LENGTH_SHORT).show();
            databaseReference.child("Registro").push().setValue(datosUsuario);
        }


    }


}

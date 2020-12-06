package com.copypaste.jzk.camerabarcodescanner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private Button btnScanner;
    private TextView tvBarCode;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference("message");


    static SQLiteDatabase myDatabase;
    Date currentTime;
    SimpleDateFormat df;
    ListView qrDetailList;
    List<QrBeanModel> QrObject = new ArrayList<>();
    QrDetailAdapter qrDetailAdapter;
    Cursor c;
    SearchView inputSearch;
    List<QrBeanModel> tempList = new ArrayList<>();

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


        intializeViews();
        qrDetailAdapter = new QrDetailAdapter( this ,R.layout.qr_list_items,QrObject);
        qrDetailList.setAdapter(qrDetailAdapter);
        qrDetailAdapter.notifyDataSetChanged();
        performSql();

    }

    public void intializeViews()
    {
        qrDetailList = (ListView) findViewById(R.id.qrDetailList);
        qrDetailList.setTextFilterEnabled(true);
        inputSearch = (SearchView) findViewById(R.id.inputSearch);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
            if (result.getContents() != null)
            {
                String codig = result.getContents();
                tvBarCode.setText("El código de barras es:\n" + codig);
                currentTime = Calendar.getInstance().getTime();
                df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(currentTime);
                String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                System.out.println(formattedDate);
                String sql = "INSERT INTO lastfourth (name, date , spec) VALUES (? , ?, ?)";
                SQLiteStatement statement = myDatabase.compileStatement(sql);
                QrBeanModel qrBeanModel = new QrBeanModel(codig,formattedDate,d);
                QrObject.add(qrBeanModel);
                qrDetailAdapter.notifyDataSetChanged();
                statement.bindString(1,codig);
                statement.bindString(2,formattedDate);
                statement.bindString(3,d);
                statement.execute();
                cargarDatos(codig);
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


            //finish();
        }
    };

    private void cargarDatos(final String codig)
    {
        Map<String, Object> datosUsuario = new HashMap<>();

        datosUsuario.put("Codigo", codig);
        //Toast.makeText(getApplicationContext(), "Registro satisfactorio", Toast.LENGTH_SHORT).show();

        //if (result.equals(""))
        //{
         //   Toast.makeText(getApplicationContext(), "No se pudo Escanear", Toast.LENGTH_SHORT).show();
        //} else {
            Toast.makeText(getApplicationContext(), "Si se pudo escanear ", Toast.LENGTH_SHORT).show();
            databaseReference.child("Escaneo").push().setValue(datosUsuario);
        }

    public void performSql()
    {
        myDatabase = this .openOrCreateDatabase( "Users" , MODE_PRIVATE , null );

        //Creating the table if not exists
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS lastfourth (name VARCHAR , date VARCHAR , spec VARCHAR)");
        showDatabaseInList();
    }
    public void showDatabaseInList()
    {

        try {
            c = myDatabase.rawQuery("SELECT * FROM lastfourth",null);

            int nameIndex = c.getColumnIndex("name");
            int dateIndex = c.getColumnIndex("date");
            int dateIDIndex = c.getColumnIndex("spec");

            if(c.moveToFirst()){
                do{
                    Log. i ( "user-name" ,c.getString(nameIndex));
                    Log.i("date id ",c.getString(dateIDIndex));
                    QrBeanModel qrBeanModel = new QrBeanModel(c.getString(nameIndex),c.getString(dateIndex),c.getString(dateIDIndex));
                    QrObject.add(qrBeanModel);
                    qrDetailAdapter.notifyDataSetChanged();
                    Log. i ( "user-age" ,c.getString(dateIndex));
                }while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
    public static void sendUniqueKey(String s)
    {
        String sql = "DELETE FROM lastfourth WHERE spec = ? ";
        SQLiteStatement statement = myDatabase.compileStatement(sql);
        statement.bindString(1,s);
        statement.execute();
        Log.i("tag",s+"Deleted");
        // MainActivity m = new MainActivity();
        // m.performSql();
    }

}

package com.example.lab_audio;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listaAudio;
    ArrayAdapter<String> audioArrayAdapter;
    String songs[];
    ArrayList<File> audio;
    private MediaRecorder grabacion;
    private String audiograbado = null;
    private Button btn_recorder;
    private Intent intentllamada;
    private static final int SOLICITUD_PERMISO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciar_controles();
    }

    public void iniciar_controles() {
        //Inicializamos las variables
        btn_recorder = (Button) findViewById(R.id.btn_grabar);
        listaAudio = findViewById(R.id.Lista);
        grabar_audio();
        listar_audio();
    }

    private void listar_audio() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            audio = buscarAudio(Environment.getExternalStorageDirectory());//inicializamos el ArrayList y el vector
            songs = new String[audio.size()];
            for (int i = 0; i < audio.size(); i++) {//Hace el recorrido en la memoria
                songs[i] = audio.get(i).getName();
            }

            audioArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, songs);
            listaAudio.setAdapter(audioArrayAdapter);
            //Hacemos un set para que podamos dar click en la lista de audio que se visualiza
            listaAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent player = new Intent(MainActivity.this, Player.class);
                    player.putExtra("songFileList", audio);//Se envia la posicion y la musica eleccionada
                    player.putExtra("position", position);
                    startActivity(player);
                }
            });
        } else {
            explicarUsoPermiso();
            solicitudPermisoLlamada();
        }
    }

    private void solicitudPermisoLlamada() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SOLICITUD_PERMISO);
        Toast.makeText(this, "pedimos el permiso", Toast.LENGTH_SHORT).show();
    }

    private void explicarUsoPermiso() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Explicamos permisos", Toast.LENGTH_SHORT).show();
            alertDialogoBasico();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SOLICITUD_PERMISO) {

            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();

        }
    }

    private ArrayList<File> buscarAudio(File file) {
        //Declaramos un ArrayList y un arreglo tipo Archivo
        ArrayList<File> audioArchivo = new ArrayList<>();
        File[] files = file.listFiles();

        for (File audioEncontrado : files) {
            if (audioEncontrado.isDirectory()) {
                audioArchivo.addAll(buscarAudio(audioEncontrado));
            } else {
                //Especificamos la extencion que tiene los archivos para agregarlo al ArrayList
                if (audioEncontrado.getName().endsWith(".mp3")) {
                    audioArchivo.add(audioEncontrado);
                }
            }
        }
        //Retornamos el ArrayList
        return audioArchivo;
    }

    private void grabar_audio() {
        //Verifica que los permisos esten escritos en el Manisfets
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //Muestra las ventanas emergentes para los permisos
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
        }
    }

    public void Grabar(View view) {
        if (grabacion == null) {
            //Guardamos el audio en un directorio en el dispositivo...
            audiograbado = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Grabacion.mp3";
            grabacion = new MediaRecorder();
            grabacion.setAudioSource(MediaRecorder.AudioSource.MIC); //Obtenemos la fuente del audio mediante el microfono
            grabacion.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //Declaramos el formato de salida del audio
            grabacion.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB); // Define la codificacion del audio
            grabacion.setOutputFile(audiograbado); //Guardamos la grabacion en nuestro archivo de salida
            try {
                grabacion.prepare();
                grabacion.start();
            } catch (IOException e) {
            }
            btn_recorder.setBackgroundResource(R.drawable.rec); //Cambiamos el boton a rojo para cuando este grabando
            Toast.makeText(getApplicationContext(), "Grabando audio", Toast.LENGTH_SHORT).show();
        } else if (grabacion != null) {
            grabacion.stop();
            grabacion.release();
            grabacion = null; //Colocamos nuevamente a su valor inicial
            btn_recorder.setBackgroundResource(R.drawable.stop_rec); //Cambiamos el boton a blanco para cuando no este grabando
            Toast.makeText(getApplicationContext(), "Grabación terminada", Toast.LENGTH_SHORT).show();
        }

    }

    public void Reproducir(View view) {
        MediaPlayer media = new MediaPlayer(); //Esta clase nos permite reproducir audio largos
        try {
            media.setDataSource(audiograbado); //Guardamos el archivo que deseamos reproducir
            media.prepare();
        } catch (IOException e) {
        }
        media.start(); //Metodo para reproducir
        Toast.makeText(getApplicationContext(), "Reproduciendo el audio", Toast.LENGTH_SHORT).show();
    }

   /* private void listar_audio() {
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                audio = buscarAudio(Environment.getExternalStorageDirectory());//inicializamos el ArrayList y el vector
                songs = new String[audio.size()];

                for (int i = 0; i< audio.size(); i++){//Hace el recorrido en la memoria
                    songs[i]= audio.get(i).getName();
                }

                audioArrayAdapter =new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,songs);
                listaAudio.setAdapter(audioArrayAdapter);
                //Hacemos un set para que podamos dar click en la lista de audio que se visualiza
                listaAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent player = new Intent(MainActivity.this,Player.class);
                        player.putExtra("songFileList", audio);//Se envia la posicion y la musica eleccionada
                        player.putExtra("position",position);
                        startActivity(player);
                    }
                });
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }


*/



    public void alertDialogoBasico() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sin el permiso, no podemos realizar acceder a sus archivos audio");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });

    }
}
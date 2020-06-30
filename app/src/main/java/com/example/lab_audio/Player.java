package com.example.lab_audio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Player extends AppCompatActivity {
    Bundle songExtraData;
    ArrayList<File> listaArchivoAudio;
    int position;
    SeekBar mSeekbar;
    TextView mTitulo;
    ImageView btplay,btnext,btprev;
    static MediaPlayer mMediaPlayer;
    TextView tiempo_co, tiempo_tot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        inicializarcontroles();
    }

    //Metodo para inicializar las variables
    public void inicializarcontroles(){
        //Inicializamos las variables
        mSeekbar = findViewById(R.id.barra_musica);
        mTitulo = findViewById(R.id.titulo_musica);
        btplay = findViewById(R.id.bt_play);
        btnext = findViewById(R.id.bt_next);
        btprev = findViewById(R.id.bt_prev);
        tiempo_co = findViewById(R.id.tiempo_corrido);
        tiempo_tot = findViewById(R.id.tiempo_total);

        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }
        //Inicializamos
        Intent songData = getIntent();
        songExtraData = songData.getExtras();
        //Recibimos del  Main Activity la posicion y musica seleccionada
        listaArchivoAudio = (ArrayList) songExtraData.getParcelableArrayList("songFileList");
        position = songExtraData.getInt("position",0);
        iniciomusica(position);

        btplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        btnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < listaArchivoAudio.size() -1 ){
                    position++;
                }else{
                    position = 0;
                }

                iniciomusica(position);
            }
        });

        btprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position <= 0){
                    position = listaArchivoAudio.size() -1;
                }else{
                    position--;
                }

                iniciomusica(position);
            }
        });
    }


    //Metodo para reproducir el audio seleccionado
    private void iniciomusica(final int position){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.reset();
        }

        String name = listaArchivoAudio.get(position).getName();
        mTitulo.setText(name);
       //Se obtiene la ruta de la cancion en la memoria
        Uri sonResourceUri = Uri.parse(listaArchivoAudio.get(position).toString());
        //Se crea el Mediaplayer
        mMediaPlayer = MediaPlayer.create(getApplicationContext(),sonResourceUri);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mSeekbar.setMax(mMediaPlayer.getDuration());

                String totTime = tiempoduracion(mMediaPlayer.getDuration());
                tiempo_tot.setText(totTime);
              //Empezar la musica
                mMediaPlayer.start();

                btplay.setImageResource(R.drawable.pausa);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // btplay.setImageResource(R.drawable.play);
                int currentsongposition = position;
                if (currentsongposition < listaArchivoAudio.size() -1 ){
        //comprobar si la posición actual de la canción en la lista es menor que la canción total presente en la lista
        //aumentar la posición por uno para reproducir la siguiente canción en la lista
                    currentsongposition++;
                }else{
                   // si la posición es mayor o igual al número de canciones en la lista, establecer la posición a cero
                    currentsongposition = 0;
                }
             //Reproducir la cancion en la lista con su posicion
                iniciomusica(currentsongposition);
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               // hacer algo cuando el SeekBar cambió
                if (fromUser){
                    mMediaPlayer.seekTo(progress);// Ver musica
                    mSeekbar.setProgress(progress);//Ver el progreso del SeekBar
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
       // configurar SeekBar para cambiar con música
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mMediaPlayer != null) {
                    try {
                        if (mMediaPlayer.isPlaying()) {
                            Message message = new Message();
                            message.what = mMediaPlayer.getCurrentPosition();
                            handler.sendMessage(message);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
   // crear handler para ver el progreso
    @SuppressLint("HandlerLeak") private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){

            tiempo_co.setText(tiempoduracion(msg.what));
            mSeekbar.setProgress(msg.what);
        }
    };

   //Metodo para altenar las imagenes play y pause
    private void play(){
       // Si la musica se detiene, cambia la imagen a play
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            btplay.setImageResource(R.drawable.play);
            // en caso contrario se mostrara la imagen pause
        }else{
            mMediaPlayer.start();
            btplay.setImageResource(R.drawable.pausa);
        }
    }

    //Metodo para saber la duracion y el tiempo transcurrido del audio seleccionado
    public String tiempoduracion(int duration){
        String timerlabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timerlabel += min + ":";

        if (sec<10) timerlabel += "0";
        timerlabel += sec ;

        return timerlabel;

    }
}
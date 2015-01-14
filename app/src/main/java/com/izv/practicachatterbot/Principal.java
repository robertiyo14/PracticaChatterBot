package com.izv.practicachatterbot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Logger;

public class Principal extends Activity implements TextToSpeech.OnInitListener{

    ChatterBotFactory factory;
    ChatterBot bot1;
    ChatterBotSession bot1Session;
    Button btTalk;
    private TextView et;
    private boolean reproductor = false;
    private static final int CTE = 1, CTE2 = 2;
    private TextToSpeech tts;
    String frase = "";
    int tono,velocidad;
    SeekBar sbTono, sbVelocidad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Intent i = new Intent();
        et = (TextView)findViewById(R.id.textView);
        sbTono = (SeekBar)findViewById(R.id.sbTono);
        sbVelocidad = (SeekBar)findViewById(R.id.sbVelocidad);
        i.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(i, CTE);
        factory = new ChatterBotFactory();
        btTalk = (Button)findViewById(R.id.btTalk);
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bot1Session = bot1.createSession();
    }

    public void talk(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Habla ahora");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        startActivityForResult(i, CTE2);
        tts.setPitch(sbTono.getProgress()-5);
        tts.setSpeechRate(sbVelocidad.getProgress()/10);
        Log.v("AAAAA: ","He recogido la frase -- "+frase);
        Log.v("AAAAA: ","VELOCIDAD "+sbVelocidad.getProgress()/10);
        Log.v("AAAAA: ","TONO "+(sbTono.getProgress()-5));
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            //Se puede reproducir
            reproductor = true;
            tts.setLanguage(new Locale("es", "ES"));
            tts.setPitch(tono);
            tts.setSpeechRate(velocidad);
            Log.v("AAAAA: ","Pongo el tono: "+tono+" y la velocidad: "+velocidad);
        } else {
            //No se puede reproducir
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CTE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this,this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        } else if(requestCode == CTE2){
            if(resultCode == RESULT_OK) {
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                frase = textos.get(0);
                btTalk.setClickable(false);
                HiloFacil hf = new HiloFacil();
                hf.execute();
                Log.v("AAAAA1: ",frase);
            }
        }
    }

    class HiloFacil extends AsyncTask<Object, Integer, String> {

        HiloFacil(String... p) {
            //Lo primero que se ejecuta.
        }

        @Override
        protected void onPreExecute() {
            //1º en ejecutarse despues del execute. Se ejecuta en la hebra UI.
            //trabajo previo.
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object[] params) {
            //2º en ejecutarse. En una hebra nueva.
            //btTalk.setVisibility(View.INVISIBLE);
            String respuesta ="";
            try {
                respuesta = bot1Session.think((frase));
                Log.v("AAAAA: ","He pensado una respuesta --"+respuesta);
            } catch (Exception ex) {
                Log.v("AAAA: ", ex.toString());
            }

//            btTalk.setVisibility(View.VISIBLE);
            return respuesta;
        }

        @Override
        protected void onPostExecute(String s) {
            //4º en ejecutarse al finalizar la hebra. En la hebra UI.
            super.onPostExecute(s);
            if(reproductor){
                tts.speak(s, TextToSpeech.QUEUE_ADD, null);
                et.setText(s);
                btTalk.setClickable(true);
                Log.v("AAAAA: ","He dicho la respuesta --"+s);
            } else {
            }
            frase = "";
        }
    }//Fin AsinkTask

    public void esp(View v){
        tts.setLanguage(new Locale("es", "ES"));
    }

    public void fr(View v){
        tts.setLanguage(Locale.FRENCH);
    }

    public void en(View v){
        tts.setLanguage(Locale.UK);
    }

    public void it(View v){
        tts.setLanguage(Locale.ITALIAN);
    }
}

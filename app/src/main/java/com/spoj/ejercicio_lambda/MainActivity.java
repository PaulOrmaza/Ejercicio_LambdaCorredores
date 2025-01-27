package com.spoj.ejercicio_lambda;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView txt_resultado_suma;
    private EditText edit_num_a, edit_num_b;
    private Button btn_suma_simple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular las referencias con los elementos del layout
        txt_resultado_suma = findViewById(R.id.txt_resultado_suma);
        edit_num_a = findViewById(R.id.edit_num_a);
        edit_num_b = findViewById(R.id.edit_num_b);
        btn_suma_simple = findViewById(R.id.btn_suma_simple);

        // Configuración del botón para realizar la solicitud
        btn_suma_simple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText y realizar la suma
                String numAString = edit_num_a.getText().toString();
                String numBString = edit_num_b.getText().toString();

                // Verificar si los campos están vacíos
                if (numAString.isEmpty() || numBString.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor ingresa ambos números", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convertir los valores a enteros
                int numA = Integer.parseInt(numAString);
                int numB = Integer.parseInt(numBString);

                // Llamar al método para realizar la suma
                realizarSumaSimple(numA, numB);
            }
        });
    }

    // Método que realiza la solicitud al servidor
    private void realizarSumaSimple(final int numA, final int numB) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Crear URL y abrir conexión
                    URL url = new URL("https://vypad3q763.execute-api.us-east-2.amazonaws.com/produccion/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8"); // Tipo de contenido
                    connection.setRequestProperty("Accept", "application/json"); // Aceptar respuesta JSON
                    connection.setDoOutput(true); // Permitir enviar datos en el cuerpo

                    // Crear cuerpo JSON
                    String jsonInputString = "{\"a\": " + numA + ", \"b\": " + numB + "}";

                    // Enviar el JSON al servidor
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Leer la respuesta
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) { // Código 200 significa éxito
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                        StringBuilder respuesta = new StringBuilder();
                        String linea;

                        while ((linea = reader.readLine()) != null) {
                            respuesta.append(linea.trim());
                        }
                        reader.close();

                        // Parsear respuesta JSON
                        JSONObject jsonResponse = new JSONObject(respuesta.toString());
                        final String resultado = jsonResponse.getString("body");

                        // Actualizar la interfaz con el resultado
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_resultado_suma.setText("Resultado: " + resultado);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error del servidor: Código " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        hilo.start(); // Iniciar el hilo
    }
}

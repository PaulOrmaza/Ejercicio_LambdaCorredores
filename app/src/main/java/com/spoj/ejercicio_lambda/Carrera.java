package com.spoj.ejercicio_lambda;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Carrera extends AppCompatActivity {

    private EditText editNumCorredores, editDistancia;
    private Button btnSimularCarrera;
    private TextView txtResultadoGeneral;
    private LinearLayout layoutResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrera);  // Asegúrate de que este es el archivo correcto.

        // Vincular las referencias con los elementos del layout
        editNumCorredores = findViewById(R.id.edit_num_corredores);  // Asegúrate de que este ID coincida
        editDistancia = findViewById(R.id.edit_distancia);  // Asegúrate de que este ID coincida
        btnSimularCarrera = findViewById(R.id.btn_simular_carrera);  // Asegúrate de que este ID coincida
        txtResultadoGeneral = findViewById(R.id.txt_resultado_general);  // Asegúrate de que este ID coincida
        layoutResultados = findViewById(R.id.layout_resultados);  // Asegúrate de que este ID coincida

        // Configuración del botón para realizar la solicitud de simulación de carrera
        btnSimularCarrera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText
                String numCorredoresStr = editNumCorredores.getText().toString();
                String distanciaStr = editDistancia.getText().toString();

                // Verificar si los campos están vacíos
                if (numCorredoresStr.isEmpty() || distanciaStr.isEmpty()) {
                    Toast.makeText(Carrera.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convertir los valores a enteros
                int numCorredores = Integer.parseInt(numCorredoresStr);
                int distancia = Integer.parseInt(distanciaStr);

                // Llamar al método para realizar la simulación
                simularCarrera(numCorredores, distancia);
            }
        });
    }

    // Método que realiza la solicitud al servidor para simular la carrera
    private void simularCarrera(final int numCorredores, final int distancia) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Crear URL y abrir conexión con la API de simulación
                    URL url = new URL("https://jkcs1rkfi1.execute-api.us-east-2.amazonaws.com/SPOJ_CARRERA/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8"); // Tipo de contenido
                    connection.setRequestProperty("Accept", "application/json"); // Aceptar respuesta JSON
                    connection.setDoOutput(true); // Permitir enviar datos en el cuerpo

                    // Crear cuerpo JSON con la estructura correcta: {"body": "{\"numCorredores\": 5, \"distancia\": 1000}"}
                    String jsonInputString = "{\"body\": \"{\\\"numCorredores\\\": " + numCorredores + ", \\\"distancia\\\": " + distancia + "}\"}";

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
                                txtResultadoGeneral.setText("Resultado: " + resultado);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Carrera.this, "Error del servidor: Código " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Carrera.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        hilo.start(); // Iniciar el hilo
    }

}

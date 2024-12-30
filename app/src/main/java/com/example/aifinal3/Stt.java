package com.example.aifinal3;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Stt {
    private Model model;
    private Recognizer recognizer;
    private Context context;
    private StringBuilder resultText;
    private static final String TAG = "Stt";
    public JSONObject re;
    private PublicInterface pi;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    

    public Stt(Context c) {
        this.context = c;
        initModel(); // Initialize model when creating the instance
    }

    private void initModel() {
        StorageService.unpack(context, "vosk-model-small-en-in-0.4", "model",
            modelFromCallback -> {
                model = modelFromCallback;  // Initialize model from unpacked path
                Log.d(TAG, "Model unpacked successfully: " + model);
                try {
                    recognizer = new Recognizer(model, 16000.0f);  // Initialize recognizer for 16kHz audio
                    Log.d(TAG, "Recognizer initialized successfully");
                } catch (IOException e) {
                    Log.e(TAG, "Recognizer initialization failed", e);
                    e.printStackTrace();
                }
            },
            exception -> {
                Log.e(TAG, "Model unpacking failed", exception);
                exception.printStackTrace();
            }
        );
    }

    public void processPcmData(byte[] pcmData, int bytesRead) {
        if (recognizer == null) {
            Log.e(TAG, "Recognizer not initialized");
            
        }

        executorService.execute(() -> {
            try {
                if (recognizer.acceptWaveForm(pcmData, bytesRead)) {
                    // Get the partial result as JSON
                    JSONObject partialResult = new JSONObject(recognizer.getResult());
                    String partialText = partialResult.optString("text", "");
                    Log.d(TAG, "Partial Result: " + partialText);

                    // Notify listener of partial results
                    if (pi != null) {
                        pi.onSttdata(partialText);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON result", e);
            }
        });
    }

    public void finalizeRecognition() {
        if (recognizer == null) return;

        executorService.execute(() -> {
            try {
                // Get the final recognition result
                re = new JSONObject(recognizer.getFinalResult());
                String finalText = re.optString("text", "");
                Log.d(TAG, "Final Result: " + finalText);

                if (pi != null) {
                    pi.onSttdata(finalText);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing final JSON result", e);
            }
        });
    }

    public void setListener(PublicInterface listener) {
        this.pi = listener;
    }

    public void cleanup() {
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        if (model != null) {
            model.close();
            model = null;
        }
        executorService.shutdown();
    }
}
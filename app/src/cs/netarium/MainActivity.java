package cs.netarium;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Подключаем нашу разметку
        setContentView(R.layout.activity_main);

        // Находим TextView, которым нужно задать текст из кода
        TextView text3 = findViewById(R.id.text3);
        TextView text4 = findViewById(R.id.text4);

        // Задаем текст из кода
        text3.setText("Третья строка задана из Java кода");
        text4.setText("Четвертая строка тоже из Java кода");
    }
}
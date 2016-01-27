package jp.ac.it_college.std.s14007.android.bluetoothmessenger;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NameOption extends Activity {

    public String getName() {
        return name;
    }

    private String name = "guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_name_option);
        EditText newName = (EditText)findViewById(R.id.name);
        name = newName.toString();

        Button setName = (Button)findViewById(R.id.set_name);
        setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText newName = (EditText) findViewById(R.id.name);
                name = newName.toString();
                Toast.makeText(NameOption.this, "名前を設定しました", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}

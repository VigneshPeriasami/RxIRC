package com.github.vignesh_iopex.ircapp.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.vignesh_iopex.ircapp.R;
import com.github.vignesh_iopex.ircapp.ui.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_content, new MainFragment()).commit();
    }
  }
}

package com.tadawistream;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class init extends Activity
{
    EditText editText1;
    Button button1;
    Button button2;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        editText1=(EditText)findViewById(R.id.IP);
        button1=(Button)findViewById(R.id.okBut);
        button2=(Button)findViewById(R.id.cancelBut);

        button1.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0) {
                String message=editText1.getText().toString();
                intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra("IP_Add",message);
                setResult(RESULT_OK,intent);
                finish();//finishing activity
            }
        });
        button2.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                try
                {
                    finishAffinity();//finishing activity
                    System.exit(0);

                } catch (Throwable e) {
                    System.out.println(e.toString());
                }
            }
        });
    }
}
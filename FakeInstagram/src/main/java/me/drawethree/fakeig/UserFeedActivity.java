package me.drawethree.fakeig;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class UserFeedActivity extends AppCompatActivity {

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        linearLayout = findViewById(R.id.linearLayout);

        Intent i = getIntent();
        String userSelected = i.getStringExtra("username");

        setTitle(userSelected + "'s Feed");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("image");
        query.whereEqualTo("username", userSelected);
        query.orderByAscending("createdAt");

        query.findInBackground((objects, e) -> {
            if (e == null) {
            for (ParseObject obj : objects) {
                ParseFile file = (ParseFile) obj.get("image");
                file.getDataInBackground((data, e1) -> {
                    if (e1 == null && data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0, data.length);
                        ImageView imageView = new ImageView(this);
                        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        imageView.setImageBitmap(bitmap);
                        linearLayout.addView(imageView);
                    }
                });
            }
            }
        });

    }
}

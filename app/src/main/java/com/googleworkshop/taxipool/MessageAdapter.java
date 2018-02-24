package com.googleworkshop.taxipool;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * This class is an adapter for displaying messages used by the chat feature. Given a ChatMessage object it can define a corresponding View object
 * that can be displayed on-screen.
 */

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;
    private String userId;

    public MessageAdapter(Context context, int resource, List<ChatMessage> objects, String userId) {
        super(context, resource, objects);
        this.context=context;
        this.userId = userId;//Current user ID
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ChatMessage message = getItem(position);
        assert message != null;
        if(message.getAuthorId().equals(userId)){//This is an incoming message
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_sent1, parent, false);
        }
        else {//This is an outgoing message
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_received1, parent, false);
        }


        TextView messageTextView = (TextView) convertView.findViewById(R.id.item_message_body_text_view);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ImageView profileImage = (ImageView) convertView.findViewById(R.id.profileImage);


        messageTextView.setVisibility(View.VISIBLE);
        messageTextView.setText(message.getText());
        authorTextView.setText(message.getName().split(" ")[0]);
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(context).load(message.getPhotoUrl()).apply(options).into(profileImage);
        return convertView;
    }
}

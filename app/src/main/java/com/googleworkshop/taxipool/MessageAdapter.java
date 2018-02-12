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

public class MessageAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;
    private String userId;

    public MessageAdapter(Context context, int resource, List<ChatMessage> objects, String userId) {
        super(context, resource, objects);
        this.context=context;
        this.userId = userId;//Current(!) user ID
    }

    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ImageView profileImage = (ImageView) convertView.findViewById(R.id.profileImage);

        ChatMessage message = getItem(position);
        messageTextView.setVisibility(View.VISIBLE);
        messageTextView.setText(message.getText());
        authorTextView.setText(message.getName());
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(context).load(message.getPhotoUrl()).apply(options).into(profileImage);
        return convertView;
    }
    */

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        //if (convertView == null) {
            //convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_sent, parent, false);
        //}
        ChatMessage message = getItem(position);
        assert message != null;
        if(message.getAuthorId().equals(userId)){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_sent1, parent, false);
        }
        else {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message_received1, parent, false);
        }


        TextView messageTextView = (TextView) convertView.findViewById(R.id.item_message_body_text_view);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        ImageView profileImage = (ImageView) convertView.findViewById(R.id.profileImage);


        messageTextView.setVisibility(View.VISIBLE);
        messageTextView.setText(message.getText());
        authorTextView.setText(message.getName());
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(context).load(message.getPhotoUrl()).apply(options).into(profileImage);
        return convertView;
    }
}

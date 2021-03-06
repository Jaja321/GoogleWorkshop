/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googleworkshop.taxipool;

/**
 * This class defines a message object that is used by the chat feature
 */

public class ChatMessage {

    private String text;//message body
    private String name;//sender's name
    private String authorId;//sender's Id
    private String photoUrl;//sender's profile picture

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, String userId, String photoUrl) {
        this.text = text;
        this.name = name.split(" ")[0];
        this.authorId=userId;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

}

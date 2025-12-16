package com.mycompany.clipper.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class emojiTabsController {

    public static class Emoji {
        private String character;
        private String name;
        private String group;
        private String slug;

        public Emoji(String character, String name, String group, String slug) {
            this.character = character;
            this.name = name;
            this.group = group;
            this.slug = slug;
        }

        public String getCharacter() {
            return character;
        }

        public String getName() {
            return name;
        }

        public String getGroup() {
            return group;
        }
    }

    public Map<String, List<Emoji>> getEmojisByGroup() {
        Map<String, List<Emoji>> groupedEmojis = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = getClass().getResourceAsStream("/assets/emojis.json")) {
            if (is == null) {
                System.err.println("Could not find /assets/emojis.json");
                return groupedEmojis;
            }

            JsonNode root = mapper.readTree(is);
            Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String emojiChar = entry.getKey();
                JsonNode details = entry.getValue();

                String name = details.has("name") ? details.get("name").asText() : "";
                String group = details.has("group") ? details.get("group").asText() : "Other";
                String slug = details.has("slug") ? details.get("slug").asText() : "";

                Emoji emoji = new Emoji(emojiChar, name, group, slug);
                groupedEmojis.computeIfAbsent(group, k -> new ArrayList<>()).add(emoji);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupedEmojis;
    }
}

package com.github.pinmacaroon.dchook.conf;

import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ModConfigProvider implements SimpleConfig.DefaultConfig {

    private String configContents = "";

    public List<Pair> getConfigsList() {
        return configsList;
    }

    private final List<Pair> configsList = new ArrayList<>();

    /**
     * @return every setting as key -> its lines in the file, in file order
     */
    public Map<String, String> getEntries() {
        return entries;
    }

    private final Map<String, String> entries = new LinkedHashMap<>();

    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment) {
        configsList.add(keyValuePair);
        String lines =
                "# " + comment + "\n" +
                "# default: " + keyValuePair.getSecond() + "\n" +
                keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + "\n";
        entries.put(keyValuePair.getFirst(), lines);
        configContents += lines;
    }

    public void addDocumentationLine(String comment) {
        configContents += "# " + comment + "\n";
    }

    public void addBlankLine() {
        configContents += "\n";
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}

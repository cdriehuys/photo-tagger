package com.unc.driehuys.chathan.phototagger;


class ImageEntry {
    private String path;
    private String tags;

    ImageEntry(String path, String tags) {
        this.path = path;
        this.tags = tags;
    }

    String getPath() {
        return path;
    }

    String getTags() {
        return tags;
    }
}

package com.esp.elasticsearchpoc;

import lombok.Data;

@Data
public class Document {

    private String id;
    private String count;
    private String isbn;
    private String authors;
    private String publishingYear;
    private String originalTitle;
    private String title;
    private String language;
    private String averageRating;
}

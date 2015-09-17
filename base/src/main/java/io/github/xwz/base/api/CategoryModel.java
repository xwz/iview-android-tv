package io.github.xwz.base.api;

public class CategoryModel extends EpisodeBaseModel {
    private String category;

    public CategoryModel(String cat) {
        category = cat;
    }

    public String getTitle() {
        return category;
    }
}

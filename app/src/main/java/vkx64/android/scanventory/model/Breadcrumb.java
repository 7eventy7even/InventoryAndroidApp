package vkx64.android.scanventory.model;

public class Breadcrumb {
    private final String name;
    private final String groupId;

    public Breadcrumb(String name, String groupId) {
        this.name = name;
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public String getGroupId() {
        return groupId;
    }
}

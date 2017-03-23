package dataStructures;

public class SearchUser {

    private String email, name, profile_image;

    public SearchUser() {
    }

    public SearchUser(String email, String name, String profile_image) {
        this.email = email;
        this.name = name;
        this.profile_image = profile_image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }
}

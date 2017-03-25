package dataStructures;

public class SearchUser {

    private String email, name, profile_image, u_id;

    public SearchUser() {
    }

    public SearchUser(String email, String name, String profile_image, String u_id) {
        this.email = email;
        this.name = name;
        this.profile_image = profile_image;
        this.u_id = u_id;
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

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }
}

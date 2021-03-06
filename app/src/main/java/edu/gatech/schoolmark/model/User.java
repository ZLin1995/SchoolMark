package edu.gatech.schoolmark.model;



public class User {

    private String displayName;
    private int age;
    private String gender;
    private Long phoneNumber;
    private boolean isStudent;
    private String introduction;

    public User(String displayName, int age, String gender, Long phoneNumber, boolean isStudent, String introduction) {

        this.displayName = displayName;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.isStudent = isStudent;
        this.introduction = introduction;

    }

    public User() {

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean getIsStudent() {
        return isStudent;
    }

    public void setStudent(boolean student) {
        isStudent = student;
    }
}

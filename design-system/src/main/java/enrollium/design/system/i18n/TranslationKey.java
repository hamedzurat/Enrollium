package enrollium.design.system.i18n;

import lombok.Getter;


@Getter
public enum TranslationKey {
    DEV("dev_notice"), //
    // ---
    HELLO("hello"), //,
    INCREMENT("increment"),
    DECREMENT("decrement"),
    RESET("reset"),
    SELECT_LANGUAGE("select_language"),
    LOGIN("login"),
    SECTION("section"),
    SPACETIME("space_time"),
    SPACE_TIME("space_time"),
    BUTTON("button"),
    CHANGEPASSWORD("change_pass"),
    USERPAGE("user_page"),
    ADVISING("advising"),
    COURSE("COURSE"),
    COURSE_desc("COURSE_desc"),
    COURSE_table("COURSE_table"),
    COURSE_form("COURSE_form"),
    STUDENT("STUDENT"),
    TRIMESTER("TRIMESTER"),
    FACULTY("FACULTY"),
    NOTIFICATION("NOTIFICATION"),
    SUBJECT("SUBJECT"),
    PREREQUISITE("PREREQUISITE"),
    USER("USER"),
    ID("ID"),
    Status("Status"),
    ABOUT("ABOUT"),
    ForgotPassword("ForgotPassword"),
    SignUp("SignUp"),
    UserInfo("UserInfo"),
    ServerStats("ServerStats"),
    OfferedCoursePage("OfferedCoursePage"),
    SectionSelection("SectionSelection"),
    SendNotification("SendNotification"),
    RegistrationStatus("RegistrationStatus"),
    TradeSection("TradeSection"),
    WithdrawRequests("WithdrawRequests"),
    RequestWithdraw("RequestWithdraw"),
    History("History"),
    Routine("Routine"),
    Home("Home"),
    Student("Student"),
    Admin("Admin"),
    Database("Database"),
    ;
    /*
     *
     */
    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }
}

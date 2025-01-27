package enrollium.design.system.i18n;

import lombok.Getter;


@Getter
public enum TranslationKey {
    DEV("dev_notice"), //
    User_Page_Title("User_Page_Title"),
    STUDENT("STUDENT"),
    SignUp_title("SignUp_title"),
    TRIMESTER("TRIMESTER"),
    FACULTY("FACULTY"),
    ID("ID"),
    SUBJECT("SUBJECT"),
    SECTION("section"),
    ABOUT("ABOUT"),
    COURSE("COURSE"),
    Admin("Admin"),
    Home("Home"),
    Routine("Routine"),
    History("History"),
    Database("Database"),
    NOTIFICATION("NOTIFICATION"),
    SignUp("SignUp"),
    LOGIN("login"),
    ForgotPassword("ForgotPassword"),
    RESET("reset"),
    SELECT_LANGUAGE("select_language"),
    ServerStats("ServerStats"),
    UserInfo("UserInfo"),
    INCREMENT("increment"),
    DECREMENT("decrement"),
    SendNotification("SendNotification"),
    PREREQUISITE("PREREQUISITE"),
    TradeSection("TradeSection"),
    WithdrawRequests("WithdrawRequests"),
    SectionSelection("SectionSelection"),
    OfferedCoursePage("OfferedCoursePage"),
    COURSE_form("COURSE_form"),
    COURSE_table("COURSE_table"),
    SPACE_TIME("space_time"),
    RegistrationStatus("RegistrationStatus"),
    SignUp_desc("SignUp_desc"),
    RequestWithdraw("RequestWithdraw"),
    Student("Student"),
    COURSE_desc("COURSE_desc"),
    HELLO("hello"),
    Chat("chat")

    // ---

    ;
    /*
     *
     */
    private final String key;

    TranslationKey(String key) {
        this.key = key;
    }
}

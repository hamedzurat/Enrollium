package enrollium.client.page.home;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import enrollium.client.event.BrowseEvent;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URI;
import java.util.Arrays;
import java.util.List;


public class About extends BasePage {
    // Translation key for the About page
    public static final TranslationKey NAME        = TranslationKey.ABOUT;
    public static final String         TextFlowCSS = "-fx-font-size: 20px; -fx-line-spacing: 1.5;";

    public About() {
        super();

        addPageHeader();

        VBox content = new VBox();
        content.setSpacing(40);
        content.setAlignment(Pos.TOP_LEFT);

        content.getChildren().add(createDescription());
        content.getChildren().add(createTeamSection());

        addNode(content);
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }

    private VBox createDescription() {
        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(30);
        descriptionBox.setAlignment(Pos.TOP_LEFT);

        TextFlow descriptionText = new TextFlow();
        descriptionText.setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
        descriptionText.maxWidthProperty().bind(descriptionBox.widthProperty().subtract(20));

        TextFlow mainText        = createFormattedText("[b]ENROLIUM[/b] is a system, we built, to make section selection smoother and more user-friendly. We worked on both Frontend and Backend," + " focusing on giving everyone a fair chance during the selection process " + "and making the system easy to use.\n", true);
        TextFlow mainText2       = createFormattedText("This trimester’s section selection process went way better than in previous terms. Last time, everyone had a tough time with section selection, and most couldn't get their preferred slots, which really frustrated them. From these difficulties, the idea for [b]ENROLLIUM[/b] was born—a platform designed to simplify and enhance the section selection process for students.\n\n", true);
        TextFlow featureDetails  = createFormattedText("- We used [b]reactive programming[/b] to make sure the application runs smoothly and efficiently uses all cpu using green threads.\n" + "- The code is [b]modular[/b], so every feature can be tested individually and can be used in other project in no time.\n" + "- Our codebase follows a [b]rolling release[/b] approach.\n" + "- We followed modern Java language practices like [b]annotations & stream api[/b] and included [b]extensive logging[/b] for better maintenance.\n", true);
        TextFlow featureDetails2 = createFormattedText("When designing the frontend, our goal was to keep things simple but useful. Instead of flashy, decorative designs that feel like an art project, we wanted it to feel practical and reliable, seamlessly fitting into our daily workflow.\n" + "- It has [b]real-time multi-language support[/b] for better accessibility.We can customize it with different [b]color themes[/b] and [b]fonts[/b].\n" + "- The settings are super easy to use, making it a breeze to navigate.\n", true);
        TextFlow featureDetails3 = createFormattedText("- We used a [b]PostgreSQL database[/b] as our main source of truth.\n" + "- To implement client-server communication, we used a [b]modular bidirectional long-lived web socket-like RPC[/b] system.\n" + "- We added [b]rate limiting[/b] to prevent abuse and DDoS attacks.\n" + "- [b]Session manager[/b] ensures one session per user by email and password.\n\n", true);

        Card  code         = new Card();
        Label coreFeatures = new Label("Code Structure:");
        coreFeatures.getStyleClass().addAll(Styles.SUCCESS, Styles.TEXT_BOLDER, Styles.TEXT_UNDERLINED);
        code.setHeader(coreFeatures);
        code.setBody(featureDetails);

        Card  front         = new Card();
        Label coreFeatures2 = new Label("Frontend:");
        coreFeatures2.getStyleClass().addAll(Styles.SUCCESS, Styles.TEXT_BOLDER, Styles.TEXT_UNDERLINED);
        front.setHeader(coreFeatures2);
        front.setBody(featureDetails2);

        Card  back          = new Card();
        Label coreFeatures3 = new Label("Backend:");
        coreFeatures3.getStyleClass().addAll(Styles.SUCCESS, Styles.TEXT_BOLDER, Styles.TEXT_UNDERLINED);
        back.setHeader(coreFeatures3);
        back.setBody(featureDetails3);

        mainText.setStyle(TextFlowCSS);
        mainText2.setStyle(TextFlowCSS);
        coreFeatures.setStyle(TextFlowCSS);
        coreFeatures2.setStyle(TextFlowCSS);
        coreFeatures3.setStyle(TextFlowCSS);
        featureDetails2.setStyle(TextFlowCSS);
        featureDetails3.setStyle(TextFlowCSS);
        featureDetails.setStyle(TextFlowCSS);

        mainText.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        mainText2.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails2.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails3.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));

        descriptionText.getChildren().addAll(mainText, mainText2, code, front, back);
        descriptionBox.getChildren().add(descriptionText);

        return descriptionBox;
    }

    private Card createTeamSection() {
        Card about   = new Card();
        VBox teamBox = new VBox();
        teamBox.setAlignment(Pos.TOP_LEFT);

        Label teamHeader = new Label("Meet Our Amazing Team");
        teamHeader.getStyleClass().addAll(Styles.ACCENT, Styles.TEXT_BOLDER, Styles.LARGE);
        teamHeader.setStyle(TextFlowCSS);
        about.setHeader(teamHeader);

        List<TeamMember> teamMembers = Arrays.asList(new TeamMember("Hamed Zurat", "", "hhashem2330113@bscse.uiu.ac.bd", "https://github.com/hamedzurat", "https://www.linkedin.com/in/hamed-zurat/"), //
                new TeamMember("Adham Zarif", "", "azarif2330721@bscse.uiu.ac.bd", "https://github.com/adhamzarif", "https://www.linkedin.com/in/zarif-237/"), //
                new TeamMember("Saber Hassan", "", "shassan2330870@bscse.uiu.ac.bd", "https://github.com/saber-hassan", "https://www.linkedin.com/in/saber-hassan/"));

        for (TeamMember member : teamMembers) {
            Card memberCard = new Card();
            memberCard.getStyleClass().add(Styles.ELEVATED_1);
            memberCard.setMinWidth(300);

            VBox cardContent = new VBox();
            cardContent.setSpacing(10);
            cardContent.setAlignment(Pos.TOP_LEFT);

            Label nameLabel = new Label(member.name);
            nameLabel.getStyleClass().addAll(Styles.WARNING, Styles.TEXT_BOLDER);
            memberCard.setHeader(nameLabel);

            TextFlow body = new TextFlow(
                    // Email Line
                    createBoldText("Email: "), createLink(member.email, "mailto:" + member.email), new Text("🔗\n"),

                    // GitHub Line
                    createBoldText("GitHub: "), createLink(member.githubLink, member.githubLink), new Text("🔗\n"),

                    // LinkedIn Line
                    createBoldText("LinkedIn: "), createLink(member.linkedinLink, member.linkedinLink));

            memberCard.setBody(body);

            nameLabel.setStyle(TextFlowCSS);
            body.setStyle(TextFlowCSS);

            teamBox.getChildren().add(memberCard);
        }

        about.setBody(teamBox);

        return about;
    }

    private Text createBoldText(String content) {
        Text text = new Text(content);
        text.getStyleClass().addAll(Styles.TEXT_BOLDER);
        return text;
    }

    private Hyperlink createLink(String displayText, String url) {
        Hyperlink link = new Hyperlink(displayText);
        link.setOnAction(e -> DefaultEventBus.getInstance().publish(new BrowseEvent(URI.create(url))));
        return link;
    }

    private static class TeamMember {
        String name;
        String role;
        String email;
        String githubLink;
        String linkedinLink;

        TeamMember(String name, String role, String email, String githubLink, String linkedinLink) {
            this.name         = name;
            this.role         = role;
            this.email        = email;
            this.githubLink   = githubLink;
            this.linkedinLink = linkedinLink;
        }
    }
}

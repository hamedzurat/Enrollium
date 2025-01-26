package enrollium.client.page.home;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Arrays;
import java.util.List;


/**
 * About page class for the Enrollium platform.
 * Provides an engaging and detailed overview of the system's purpose and features.
 */
public class About extends BasePage {
    // Translation key for the About page
    public static final TranslationKey NAME = TranslationKey.ABOUT;
    public static final String TextFlowCSS = "-fx-font-size: 16px; -fx-line-spacing: 1.5;";

    public About() {
        super();

        addPageHeader(); // This adds a header to the page

        VBox content = new VBox();
        content.setSpacing(40);
        content.setAlignment(Pos.TOP_LEFT);

        content.getChildren().add(createDescription());
        content.getChildren().add(new Separator());
        content.getChildren().add(createTeamSection());

        addNode(content);
    }

    /**
     * Updates localized texts dynamically, ensuring the UI reflects the latest translations.
     */
    @Override
    protected void updateTexts() {
        super.updateTexts();
    }

    /**
     * Returns the name translation key for the About page.
     *
     * @return TranslationKey for the About page
     */
    @Override
    public TranslationKey getName() {
        return NAME;
    }

    /**
     * Creates the description section with left-aligned text.
     *
     * @return VBox containing the description
     */
    private VBox createDescription() {
        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(30); // Adjusted spacing between lines
        descriptionBox.setAlignment(Pos.TOP_LEFT);

        TextFlow descriptionText = new TextFlow();
        descriptionText.setTextAlignment(javafx.scene.text.TextAlignment.LEFT); // Ensure text alignment to the left
        descriptionText.maxWidthProperty().bind(descriptionBox.widthProperty().subtract(20));

        // Create formatted text sections
        TextFlow mainText = createFormattedText("[b]ENROLIUM[/b] is a system, we built, to make section selection smoother and more user-friendly. We worked on both Frontend and Backend," +
                                                " focusing on giving everyone a fair chance during the selection process " +
                                                "and making the system easy to use.\n", true);
        TextFlow mainText2 = createFormattedText("This trimester’s section selection process went way better than in previous terms. Last time, everyone had a tough time with section selection, and most couldn't get their preferred slots, which really frustrated them. From these difficulties, the idea for [b]ENROLLIUM[/b] was born—a platform designed to simplify and enhance the section selection process for students.\n\n", true);

        // Ensure proper wrapping for long text



        // Core features header
        Text coreFeatures = new Text("\n✨ Code Structure:\n");
        coreFeatures.setStyle("-fx-font-weight: bold; -fx-fill: #2b579a; -fx-font-size: 18px; -fx-line-spacing: 1.5;");

        // Core features details
        TextFlow featureDetails = createFormattedText(
                "- We used [b]reactive programming[/b] to make sure the application runs smoothly and efficiently uses all cpu using green threads.\n" +
                "- The code is [b]modular[/b], so every feature can be tested individually and can be used in other project in no time.\n" +
                "- Our codebase follows a [b]rolling release[/b] approach.\n"+
                "- We followed modern Java language practices like [b]annotations & stream api[/b] and included [b]extensive logging[/b] for better maintenance.\n", true);

        Text coreFeatures2 = new Text("\n✨ Frontend:\n");
        coreFeatures2.setStyle("-fx-font-weight: bold; -fx-fill: #2b579a; -fx-font-size: 18px; -fx-line-spacing: 1.5;");

        // Core features details
        TextFlow featureDetails2 = createFormattedText(
                "When designing the frontend, our goal was to keep things simple but useful. Instead of flashy, decorative designs that feel like an art project, we wanted it to feel practical and reliable, seamlessly fitting into our daily workflow.\n"+
                "- It has [b]real-time multi-language support[/b] for better accessibility.We can customize it with different [b]color themes[/b] and [b]fonts[/b].\n" +
                "- The settings are super easy to use, making it a breeze to navigate.\n", true);
         Text coreFeatures3 = new Text("\n✨ Backend:\n");
        coreFeatures3.setStyle("-fx-font-weight: bold; -fx-fill: #2b579a; -fx-font-size: 18px; -fx-line-spacing: 1.5;");

        // Core features details
        TextFlow featureDetails3 = createFormattedText(

                "- We used a [b]PostgreSQL database[/b] as our main source of truth.\n" +
                "- To implement client-server communication, we used a [b]modular bidirectional long-lived web socket-like RPC[/b] system.\n" +
                "- We added [b]rate limiting[/b] to prevent abuse and DDoS attacks.\n"+
                "- [b]Session manager[/b] ensures one session per user by email and password.\n\n", true);

        mainText.setStyle(TextFlowCSS);
        mainText2.setStyle(TextFlowCSS);
        featureDetails2.setStyle(TextFlowCSS);
        featureDetails3.setStyle(TextFlowCSS);
        featureDetails.setStyle(TextFlowCSS);

        mainText.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        mainText2.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails2.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));
        featureDetails3.prefWidthProperty().bind(descriptionBox.widthProperty().subtract(20));


        descriptionText.getChildren().addAll(mainText, mainText2, coreFeatures, featureDetails,coreFeatures2, featureDetails2,coreFeatures3, featureDetails3);
        descriptionBox.getChildren().add(descriptionText);

        return descriptionBox;
    }

    /**
     * Creates the team section of the About page using cards.
     *
     * @return VBox containing team member cards
     */
    private VBox createTeamSection() {
        VBox teamBox = new VBox();
        teamBox.setSpacing(30);
        teamBox.setAlignment(Pos.TOP_LEFT); // Ensure left alignment for the entire section

        Label teamHeader = new Label("🤝 Meet Our Amazing Team");
        teamHeader.setStyle("-fx-text-fill: #2b579a; -fx-font-weight: bold; -fx-font-size: 18px;");
        teamBox.getChildren().add(teamHeader);

        List<TeamMember> teamMembers = Arrays.asList(new TeamMember("Hamed Zurat", "", "hhashem2330113@bscse.uiu.ac.bd", "https://github.com/hamedzurat", "https://www.linkedin.com/in/hamed-zurat/"), new TeamMember("Adham Zarif", "", "azarif2330721@bscse.uiu.ac.bd", "github.com/adhamzarif", "https://www.linkedin.com/in/zarif-237/"), new TeamMember("Saber Hassan", "", "shassan2330870@bscse.uiu.ac.bd", "github.com/saber-hassan", "https://www.linkedin.com/in/saber-hassan/"));

        for (TeamMember member : teamMembers) {
            Card memberCard = new Card();
            memberCard.getStyleClass().add(Styles.ELEVATED_1);
            memberCard.setMinWidth(300);

            VBox cardContent = new VBox();
            cardContent.setSpacing(10);
            cardContent.setAlignment(Pos.TOP_LEFT);

            Label nameLabel = new Label(member.name);
            nameLabel.setStyle("-fx-text-fill: #2b579a; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label emailLabel = new Label("📧 Email: " + member.email);
            emailLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

//            Label githubLabel = new Label("🔗 GitHub: " + member.githubLink);
            TextFlow githubLabel = createFormattedText("🔗 GitHub: " + member.githubLink, true);
            githubLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

            Label linkedinLabel = new Label("🔗 LinkedIn: " + member.linkedinLink);
            linkedinLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

            cardContent.getChildren().addAll(nameLabel, emailLabel, githubLabel, linkedinLabel);
            memberCard.setBody(cardContent);

            teamBox.getChildren().add(memberCard);
        }

        return teamBox;
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

package enrollium.client.page.home;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.application.HostServices;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
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

    private final HostServices hostServices;

    // Constructor for the About page
    public About() {
        this(null);
    }

    public About(HostServices hostServices) {
        super();
        this.hostServices = hostServices;

        addPageHeader(); // This adds a header to the page
        setHeaderTitle("Welcome to Enrollium"); // Sets the title of the header

        VBox content = new VBox();
        content.setSpacing(40);
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20px;");

        content.getChildren().add(new Separator());
        content.getChildren().add(createDescription());
        content.getChildren().add(new Separator());
        content.getChildren().add(createBenefitsSection());
        content.getChildren().add(new Separator());
        content.getChildren().add(createTeamSection());

        addNode(content);
    }

    private void setHeaderTitle(String welcomeToEnrollium) {}

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
     * Creates the description section with centered text.
     *
     * @return VBox containing the description
     */
    private VBox createDescription() {
        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(20); // Adjusted spacing between lines
        descriptionBox.setAlignment(Pos.CENTER);

        TextFlow descriptionText = new TextFlow();
        descriptionText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); // Ensure text alignment in the center

        Text mainText = new Text(
                "Enrollium is your partner in simplifying academic management. We provide a " +
                "feature-rich platform designed to streamline course registration, optimize section selection, " +
                "and empower administrators to manage effortlessly.\n\n"
        );
        mainText.setStyle("-fx-font-size: 16px; -fx-line-spacing: 1.5;");

        Text coreFeatures = new Text("✨ Core Features:\n");
        coreFeatures.setStyle("-fx-font-weight: bold; -fx-fill: #2b579a; -fx-font-size: 18px; -fx-line-spacing: 1.5;");

        Text featureDetails = new Text(
                "- Simplified Course Registration: Register for courses in just a few clicks.\n" +
                "- Real-Time Section Updates: Stay updated with live availability for sections.\n" +
                "- Flexible Section Swaps: Easily manage schedule adjustments.\n" +
                "- Comprehensive Admin Tools: Control and monitor the system efficiently."
        );
        featureDetails.setStyle("-fx-font-size: 16px; -fx-line-spacing: 1.5;");

        descriptionText.getChildren().addAll(mainText, coreFeatures, featureDetails);
        descriptionBox.getChildren().add(descriptionText);

        return descriptionBox;
    }

    /**
     * Creates a benefits section with centered text.
     *
     * @return VBox containing the benefits
     */
    private VBox createBenefitsSection() {
        VBox benefitsBox = new VBox();
        benefitsBox.setSpacing(10); // Reduced spacing between lines
        benefitsBox.setAlignment(Pos.CENTER);

        Label benefitsHeader = new Label("🌟 Why Enrollium is Your Best Choice");
        benefitsHeader.setStyle("-fx-text-fill: #2b579a; -fx-font-weight: bold; -fx-font-size: 18px;");
        benefitsBox.getChildren().add(benefitsHeader);

        List<String> benefits = Arrays.asList(
                "✔ Effortless Course Registration: Save time and reduce complexity.",
                "✔ Real-Time Updates: Make informed decisions with live data.",
                "✔ Hassle-Free Section Management: Adjust schedules without stress.",
                "✔ Advanced Admin Controls: Ensure seamless operations.",
                "✔ Scalability and Reliability: Designed to grow with your institution."
        );

        for (String benefit : benefits) {
            Label benefitLabel = new Label(benefit);
            benefitLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 16px;");
            benefitsBox.getChildren().add(benefitLabel);
        }

        return benefitsBox;
    }

    /**
     * Creates the team section of the About page using cards.
     *
     * @return VBox containing team member cards
     */
    private VBox createTeamSection() {
        VBox teamBox = new VBox();
        teamBox.setSpacing(30);
        teamBox.setAlignment(Pos.TOP_CENTER);

        Label teamHeader = new Label("🤝 Meet Our Amazing Team");
        teamHeader.setStyle("-fx-text-fill: #2b579a; -fx-font-weight: bold; -fx-font-size: 18px;");
        teamBox.getChildren().add(teamHeader);

        List<TeamMember> teamMembers = Arrays.asList(
                new TeamMember("Alice Johnson", "Product Manager", "alice.johnson@example.com", "https://github.com/alicejohnson", "https://linkedin.com/in/alicejohnson"),
                new TeamMember("Bob Smith", "Lead Developer", "bob.smith@example.com", "https://github.com/bsmith", "https://linkedin.com/in/bsmith"),
                new TeamMember("Charlie Lee", "UI/UX Designer", "charlie.lee@example.com", "https://github.com/charlielee", "https://linkedin.com/in/charlielee")
        );

        for (TeamMember member : teamMembers) {
            Card memberCard = new Card();
            memberCard.getStyleClass().add(Styles.ELEVATED_1);
            memberCard.setMinWidth(300);

            VBox cardContent = new VBox();
            cardContent.setSpacing(10);
            cardContent.setAlignment(Pos.TOP_LEFT);

            Label nameLabel = new Label(member.name + " - " + member.role);
            nameLabel.setStyle("-fx-text-fill: #2b579a; -fx-font-size: 16px; -fx-font-weight: bold;");

            Label emailLabel = new Label("📧 Email: " + member.email);
            emailLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 14px;");

            Label githubLabel = new Label("🔗 GitHub: " + member.githubLink);
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
            this.name = name;
            this.role = role;
            this.email = email;
            this.githubLink = githubLink;
            this.linkedinLink = linkedinLink;
        }
    }
}

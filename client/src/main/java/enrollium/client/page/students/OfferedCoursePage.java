package enrollium.client.page.students;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import enrollium.client.Resources;
import enrollium.client.page.BasePage;
import enrollium.design.system.i18n.TranslationKey;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;

import java.util.List;
import java.util.Random;


public class OfferedCoursePage extends BasePage {
    public static final  TranslationKey NAME       = TranslationKey.COURSE;
    private static final String         IMAGE_PATH = "images/courses/";
    private static final Random         RANDOM     = new Random();

    public OfferedCoursePage() {
        super();

        List<OfferedCourseData> offeredCourses = List.of( //
                OfferedCourseData.builder()
                                 .imgFile("madeline-n-jVP0wxSWsHM-unsplash.jpg")
                                 .trimester("Fall 2024")
                                 .courseCode("CSE101")
                                 .titleEn("Introduction to Programming")
                                 .titleBn("প্রোগ্রামিং পরিচিতি")
                                 .descriptionEn("The focus is on developing [b]reading, writing, speaking, [/b]and[b] listening [/b]skills through strategies like skimming, scanning, brainstorming, and note-taking. Key topics include grammatical knowledge, linking words, summarizing, creative writing, and presentation techniques. Speaking and listening emphasize pronunciation, intonation, vocabulary, impromptu speaking, group presentations, and engaging with drama, famous speeches, and listening activities[code][/code]")
                                 .descriptionBn("স্কিমিং, স্ক্যানিং, ব্রেইনস্টর্মিং এবং নোট নেওয়ার মতো কৌশলগুলির মাধ্যমে [বি] পড়া, লেখা, কথা বলা, [/খ] এবং [খ] শোনার দক্ষতা বিকাশের দিকে মনোনিবেশ করা হয়েছে। মূল বিষয়গুলির মধ্যে রয়েছে ব্যাকরণগত জ্ঞান, শব্দ সংযোগ, সংক্ষিপ্তকরণ, সৃজনশীল লেখা এবং উপস্থাপনা কৌশল। স্পিকিং এবং শ্রবণ উচ্চারণ, স্বরভঙ্গি, শব্দভাণ্ডার, তাত্ক্ষণিক বক্তৃতা, গ্রুপ উপস্থাপনা এবং নাটক, বিখ্যাত বক্তৃতা এবং শ্রবণ ক্রিয়াকলাপের সাথে জড়িত থাকার উপর জোর দেয়[কোড][/কোড]")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("None")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Spring 2024")
                                 .courseCode("CSE102")
                                 .titleEn("Data Structures")
                                 .titleBn("ডেটা স্ট্রাকচার")
                                 .descriptionEn("Introduction to data structures.")
                                 .descriptionBn("ডেটা স্ট্রাকচার পরিচিতি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE101")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Summer 2024")
                                 .courseCode("CSE201")
                                 .titleEn("Algorithms")
                                 .titleBn("অ্যালগরিদম")
                                 .descriptionEn("Study efficient algorithms for problem solving.")
                                 .descriptionBn("সমস্যা সমাধানের জন্য কার্যকর অ্যালগরিদম অধ্যয়ন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE102")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Fall 2024")
                                 .courseCode("CSE202")
                                 .titleEn("Object-Oriented Programming")
                                 .titleBn("অবজেক্ট ওরিয়েন্টেড প্রোগ্রামিং")
                                 .descriptionEn("Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.Learn object-oriented concepts and design patterns.")
                                 .descriptionBn("অবজেক্ট ওরিয়েন্টেড ধারণা এবং ডিজাইন প্যাটার্ন শিখুন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE101")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Spring 2025")
                                 .courseCode("CSE301")
                                 .titleEn("Operating Systems")
                                 .titleBn("অপারেটিং সিস্টেম")
                                 .descriptionEn("Introduction to operating systems and resource management.")
                                 .descriptionBn("অপারেটিং সিস্টেম এবং রিসোর্স ম্যানেজমেন্ট পরিচিতি।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE202")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Summer 2025")
                                 .courseCode("CSE302")
                                 .titleEn("Database Management Systems")
                                 .titleBn("ডেটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Study database systems, SQL, and data modeling.")
                                 .descriptionBn("ডেটাবেস সিস্টেম, SQL, এবং ডেটা মডেলিং অধ্যয়ন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE102")
                                 .build(),
                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Summer 2025")
                                 .courseCode("CSE302")
                                 .titleEn("Database Management Systems")
                                 .titleBn("ডেটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Study database systems, SQL, and data modeling.")
                                 .descriptionBn("ডেটাবেস সিস্টেম, SQL, এবং ডেটা মডেলিং অধ্যয়ন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE102")
                                 .build(),
                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Summer 2025")
                                 .courseCode("CSE302")
                                 .titleEn("Database Management Systems")
                                 .titleBn("ডেটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Study database systems, SQL, and data modeling.")
                                 .descriptionBn("ডেটাবেস সিস্টেম, SQL, এবং ডেটা মডেলিং অধ্যয়ন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE102")
                                 .build(),
                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Summer 2025")
                                 .courseCode("CSE302")
                                 .titleEn("Database Management Systems")
                                 .titleBn("ডেটাবেস ম্যানেজমেন্ট সিস্টেম")
                                 .descriptionEn("Study database systems, SQL, and data modeling.")
                                 .descriptionBn("ডেটাবেস সিস্টেম, SQL, এবং ডেটা মডেলিং অধ্যয়ন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE102")
                                 .build(),

                OfferedCourseData.builder()
                                 .imgFile("vishnu-mohanan-kyDsOF8gsIA-unsplash.jpg")
                                 .trimester("Fall 2025")
                                 .courseCode("CSE401")
                                 .titleEn("Software Engineering")
                                 .titleBn("সফটওয়্যার ইঞ্জিনিয়ারিং")
                                 .descriptionEn("Learn principles of software development and project management.")
                                 .descriptionBn("সফটওয়্যার ডেভেলপমেন্ট এবং প্রকল্প পরিচালনার নীতিমালা শিখুন।")
                                 .type("Theory")
                                 .credits(3)
                                 .prerequisite("CSE301")
                                 .build() //
        );

        addPageHeader();
        addFormattedText("Course Offerings");

        FlowPane courseContainer = new FlowPane(30, 20);
        courseContainer.setAlignment(Pos.CENTER);
        courseContainer.setPrefWrapLength(700); // Adjust based on available space

        offeredCourses.forEach(course -> courseContainer.getChildren().add(createCourseCard(course)));

        // Wrap the FlowPane inside a ScrollPane to enable scrolling
        ScrollPane scrollPane = new ScrollPane(courseContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true); // Allow dragging with mouse
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical scrollbar as needed
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // No horizontal scrollbar

        addNode(scrollPane);
    }

    private Card createCourseCard(OfferedCourseData course) {
        var card = new Card();
        card.getStyleClass().add(Styles.ELEVATED_1);
        card.setMinWidth(350);
        card.setMaxWidth(350);

        // Load course image with cropping
        var courseImage = new ImageView(new Image(Resources.getResourceAsStream(IMAGE_PATH + course.getImgFile())));
        courseImage.setFitHeight(300);
        courseImage.setPreserveRatio(true);
        courseImage.setSmooth(true);
        courseImage.setClip(new Rectangle(350, 300)); // Crops the image to center

        // Center crop by adjusting viewport
        Image  img         = courseImage.getImage();
        double imageWidth  = img.getWidth();
        double imageHeight = img.getHeight();
        double cropWidth   = 350;  // Target width
        double cropHeight  = 300; // Target height

        if (imageWidth > cropWidth || imageHeight > cropHeight) {
            double x = (imageWidth - cropWidth) / 2;
            double y = (imageHeight - cropHeight) / 2;
            courseImage.setViewport(new Rectangle2D(x, y, cropWidth, cropHeight));
        }

        card.setSubHeader(courseImage);

        // Header with title, course code, type, and credit
        var subHeaderText = String.format("%s (%s) - %d Credits", course.getCourseCode(), course.getType(), course.getCredits());
        var header        = new Tile(course.getTitleEn(), subHeaderText);
        card.setHeader(header);

        // Body with expandable description text
        TextFlow description = createFormattedText(course.getDescriptionEn(), true);
        description.setMaxWidth(320);
        description.setPrefHeight(Region.USE_COMPUTED_SIZE);
        card.setBody(description);

        // Footer with randomized status and action button
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().addAll(
                new Circle(8, Color.web(randomColor())),
                new Label(randomStatus())
        );

        Button actionButton = new Button(determineActionButtonText(randomStatus()));
        actionButton.setPrefWidth(120);
        footer.getChildren().add(actionButton);
        card.setFooter(footer);

        return card;
    }

    private String randomStatus() {
        String[] statuses = {"SELECTED", "REGISTERED", "COMPLETED", "DROPPED"};
        return statuses[RANDOM.nextInt(statuses.length)];
    }

    private String determineActionButtonText(String status) {
        return switch (status) {
            case "SELECTED" -> "Unselect";
            case "REGISTERED" -> "Withdraw";
            case "COMPLETED", "DROPPED" -> "Retake";
            default -> "Select";
        };
    }

    private String randomColor() {
        String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#FF33A1"};
        return colors[RANDOM.nextInt(colors.length)];
    }

    @Override
    public TranslationKey getName() {
        return NAME;
    }
}

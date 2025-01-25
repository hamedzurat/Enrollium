rivate VBox createDescription() {
        VBox descriptionBox = new VBox();
        descriptionBox.setSpacing(15); // Adjusted spacing between lines
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

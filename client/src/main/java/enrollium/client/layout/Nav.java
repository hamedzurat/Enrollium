package enrollium.client.layout;

import enrollium.client.page.Page;
import enrollium.client.page.debug.Button;
import javafx.scene.Node;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;


record Nav(String title, @Nullable Node graphic, @Nullable Class<? extends Page> pageClass, @Nullable List<String> searchKeywords) {
    public static final  Nav                        ROOT         = new Nav("ROOT", null, null, null);
    private static final Set<Class<? extends Page>> TAGGED_PAGES = Set.of(Button.class);

    // Validation: Ensures that every Nav item has a title.
    // Default Handling: If no searchKeywords are provided, it defaults to an empty list.
    public Nav {
        Objects.requireNonNull(title, "title");
        searchKeywords = Objects.requireNonNullElse(searchKeywords, Collections.emptyList());
    }

    // Basically for search dialog
    public boolean matches(String filter) {
        Objects.requireNonNull(filter);
        return contains(title, filter) //
               || (searchKeywords != null //
                   && searchKeywords.stream().anyMatch(keyword -> contains(keyword, filter)));
    }

    // Case-insensitive search for both titles and keywords.
    private boolean contains(String text, String filter) {
        return text.toLowerCase().contains(filter.toLowerCase());
    }

    // Group vs. Page
    public boolean isGroup() {
        return pageClass == null;
    }

    public boolean isTagged() {
        return pageClass != null && TAGGED_PAGES.contains(pageClass);
    }
}

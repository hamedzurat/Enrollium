package enrollium.client.page.database;

import lombok.Data;


@Data
public class DropdownItem {
    private final String id;
    private final String displayText;

    @Override
    public String toString() {
        return displayText;
    }
}

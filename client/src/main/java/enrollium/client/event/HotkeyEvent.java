package enrollium.client.event;

import javafx.scene.input.KeyCodeCombination;
import lombok.Getter;


@Getter
public final class HotkeyEvent extends Event {
    private final KeyCodeCombination keys;

    public HotkeyEvent(KeyCodeCombination keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "HotkeyEvent{"
               + "keys=" + keys
               + "} " + super.toString();
    }
}

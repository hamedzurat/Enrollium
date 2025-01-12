package enrollium.client.event;

import enrollium.client.page.Page;
import lombok.Getter;


@Getter
public final class NavEvent extends Event {
    private final Class<? extends Page> page;

    public NavEvent(Class<? extends Page> page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "NavEvent{"
               + "page=" + page
               + "} " + super.toString();
    }
}

package enrollium.client.event;

import lombok.Getter;

import java.net.URI;


@Getter
public final class BrowseEvent extends Event {
    private final URI uri;

    public BrowseEvent(URI uri) {
        this.uri = uri;
    }

    public static void fire(String url) {
        Event.publish(new BrowseEvent(URI.create(url)));
    }

    @Override
    public String toString() {
        return "BrowseEvent{"
               + "uri=" + uri
               + "} " + super.toString();
    }
}

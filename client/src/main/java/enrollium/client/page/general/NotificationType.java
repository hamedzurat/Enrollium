package enrollium.client.page.general;

import atlantafx.base.theme.Styles;
import lombok.Getter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;


@Getter
public enum NotificationType {
    INFO(Styles.ACCENT, Material2OutlinedAL.INFO),
    SUCCESS(Styles.SUCCESS, Material2OutlinedAL.CHECK_CIRCLE),
    WARNING(Styles.WARNING, Material2OutlinedMZ.WARNING),
    DANGER(Styles.DANGER, Material2OutlinedAL.ERROR);
    private final String styleClass;
    private final Ikon   icon;

    NotificationType(String styleClass, Ikon icon) {
        this.styleClass = styleClass;
        this.icon       = icon;
    }
}

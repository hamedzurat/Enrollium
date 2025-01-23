package enrollium.client.page.students;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@Getter
@Builder
@ToString
public class OfferedCourseData {
    private final String       imgFile;
    private final Integer       trimester;
    private final String       courseCode;
    private final String       titleEn;
    private final String       titleBn;
    private final String       descriptionEn;
    private final String       descriptionBn;
    private final String       type;  // "theory" or "lab"
    private final int          credits;
    @Singular
    @Nullable
    private final List<String> prerequisites;
}

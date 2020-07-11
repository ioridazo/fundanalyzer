package github.com.ioridazo.fundanalyzer.domain.entity.master;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

@Entity(immutable = true)
@Table(name = "bs_subject")
public class BsSubject extends Detail {

    @Id
    private final String id;

    private final String outlineSubjectId;

    private final String detailSubjectId;

    private final String name;

    public BsSubject(
            String id,
            String outlineSubjectId,
            String detailSubjectId,
            String name) {
        this.id = id;
        this.outlineSubjectId = outlineSubjectId;
        this.detailSubjectId = detailSubjectId;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getOutlineSubjectId() {
        return outlineSubjectId;
    }

    @Override
    public String getDetailSubjectId() {
        return detailSubjectId;
    }

    @Override
    public String getName() {
        return name;
    }
}
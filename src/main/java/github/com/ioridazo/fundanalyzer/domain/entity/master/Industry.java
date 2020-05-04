package github.com.ioridazo.fundanalyzer.domain.entity.master;

import lombok.Value;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

@Value
@Entity(immutable = true)
@Table(name = "industry")
public class Industry {

    @Id
    String id;

    String name;
}
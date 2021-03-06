package github.com.ioridazo.fundanalyzer.domain.domain.entity.transaction;

import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.MinkabuResultBean;
import github.com.ioridazo.fundanalyzer.domain.util.Parser;
import lombok.Value;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SuppressWarnings("RedundantModifiersValueLombok")
@Value
@Entity(immutable = true)
@Table(name = "minkabu")
public class MinkabuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Integer id;

    private final String companyCode;

    private final LocalDate targetDate;

    private final Double stockPrice;

    private final Double goalsStock;

    private final Double theoreticalStock;

    private final Double individualInvestorsStock;

    private final Double securitiesAnalystStock;

    @Column(updatable = false)
    private final LocalDateTime createdAt;

    public static MinkabuEntity ofMinkabuResultBean(final String code, final MinkabuResultBean minkabu, final LocalDateTime createdAt) {
        LocalDate targetDate;
        try {
            targetDate = MonthDay.parse(minkabu.getTargetDate(), DateTimeFormatter.ofPattern("MM/dd")).atYear(LocalDate.now().getYear());
        } catch (DateTimeParseException e) {
            targetDate = LocalDate.now();
        }
        return new MinkabuEntity(
                null,
                code,
                targetDate,
                Parser.parseDoubleMinkabu(minkabu.getStockPrice()).orElse(null),
                Parser.parseDoubleMinkabu(minkabu.getExpectedStockPrice().getGoals()).orElse(null),
                Parser.parseDoubleMinkabu(minkabu.getExpectedStockPrice().getTheoretical()).orElse(null),
                Parser.parseDoubleMinkabu(minkabu.getExpectedStockPrice().getIndividualInvestors()).orElse(null),
                Parser.parseDoubleMinkabu(minkabu.getExpectedStockPrice().getSecuritiesAnalyst()).orElse(null),
                createdAt
        );
    }
}

package github.com.ioridazo.fundanalyzer.domain.domain.specification;

import github.com.ioridazo.fundanalyzer.domain.domain.dao.transaction.MinkabuDao;
import github.com.ioridazo.fundanalyzer.domain.domain.dao.transaction.StockPriceDao;
import github.com.ioridazo.fundanalyzer.domain.domain.entity.transaction.MinkabuEntity;
import github.com.ioridazo.fundanalyzer.domain.domain.entity.transaction.StockPriceEntity;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.Kabuoji3ResultBean;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.MinkabuResultBean;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.NikkeiResultBean;
import github.com.ioridazo.fundanalyzer.domain.value.Company;
import github.com.ioridazo.fundanalyzer.domain.value.Document;
import github.com.ioridazo.fundanalyzer.domain.value.Stock;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerNotExistException;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seasar.doma.jdbc.UniqueConstraintException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StockSpecification {

    private static final Logger log = LogManager.getLogger(StockSpecification.class);
    private static final int SECOND_DECIMAL_PLACE = 2;

    private final StockPriceDao stockPriceDao;
    private final MinkabuDao minkabuDao;
    private final DocumentSpecification documentSpecification;

    @Value("${app.config.view.edinet-list.size}")
    int lastDays;

    public StockSpecification(
            final StockPriceDao stockPriceDao,
            final MinkabuDao minkabuDao,
            final DocumentSpecification documentSpecification) {
        this.stockPriceDao = stockPriceDao;
        this.minkabuDao = minkabuDao;
        this.documentSpecification = documentSpecification;
    }

    LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 株価情報を取得する
     *
     * @param company 企業情報
     * @return 株価情報
     */
    public Stock findStock(final Company company) {
        final List<StockPriceEntity> stockPriceList = stockPriceDao.selectByCode(company.getCode().orElseThrow(FundanalyzerNotExistException::new));

        final Optional<LocalDate> importDate = stockPriceList.stream()
                .max(Comparator.comparing(StockPriceEntity::getTargetDate))
                .map(StockPriceEntity::getTargetDate);

        final Optional<BigDecimal> latestStock = stockPriceList.stream()
                .max(Comparator.comparing(StockPriceEntity::getTargetDate))
                .map(StockPriceEntity::getStockPrice)
                .map(BigDecimal::valueOf);

        final Optional<BigDecimal> averageStockPrice = averageStockPrice(company, stockPriceList);

        final Optional<BigDecimal> latestForecastStock = minkabuDao.selectByCode(company.getCode().orElseThrow(FundanalyzerNotExistException::new)).stream()
                .max(Comparator.comparing(MinkabuEntity::getTargetDate))
                .map(MinkabuEntity::getGoalsStock)
                .map(BigDecimal::new);

        return Stock.of(
                company,
                averageStockPrice.orElse(null),
                importDate.orElse(null),
                latestStock.orElse(null),
                latestForecastStock.orElse(null),
                stockPriceList,
                minkabuDao.selectByCode(company.getCode().orElseThrow(FundanalyzerNotExistException::new))
        );
    }

    /**
     * 日経から取得した株価情報を登録する
     *
     * @param code   企業コード
     * @param nikkei 日経から取得した株価情報
     */
    public void insert(final String code, final NikkeiResultBean nikkei) {
        if (isEmptyStockPrice(code, nikkei.getTargetDate())) {
            stockPriceDao.insert(StockPriceEntity.ofNikkeiResultBean(code, nikkei, nowLocalDateTime()));
        }
    }

    /**
     * kabuoji3から取得した株価情報を登録する
     *
     * @param code         企業コード
     * @param kabuoji3List kabuoji3から取得した株価情報
     */
    public void insert(final String code, final List<Kabuoji3ResultBean> kabuoji3List) {
        kabuoji3List.forEach(kabuoji3 -> {
            if (isEmptyStockPrice(code, kabuoji3.getTargetDate())) {
                try {
                    stockPriceDao.insert(StockPriceEntity.ofKabuoji3ResultBean(code, kabuoji3, nowLocalDateTime()));
                } catch (NestedRuntimeException e) {
                    if (e.contains(UniqueConstraintException.class)) {
                        log.debug("一意制約違反のため、株価情報のデータベース登録をスキップします。" +
                                "\t企業コード:{}\t対象日:{}", code, kabuoji3.getTargetDate());
                    } else {
                        throw new FundanalyzerRuntimeException("想定外のエラーが発生しました。", e);
                    }
                }
            }
        });
    }

    /**
     * みんかぶから取得した株価情報を登録する
     *
     * @param code    企業コード
     * @param minkabu みんかぶから取得した株価情報
     */
    public void insert(final String code, final MinkabuResultBean minkabu) {
        if (!isPresentMinkabu(code, minkabu.getTargetDate())) {
            minkabuDao.insert(MinkabuEntity.ofMinkabuResultBean(code, minkabu, nowLocalDateTime()));
        }
    }

    /**
     * 特定期間における平均の株価を取得する
     *
     * @param company        企業情報
     * @param stockPriceList 株価情報リスト
     * @return 平均の株価
     */
    private Optional<BigDecimal> averageStockPrice(final Company company, final List<StockPriceEntity> stockPriceList) {
        final Optional<LocalDate> submitDate = documentSpecification.latestDocument(company).map(Document::getSubmitDate);

        if (submitDate.isEmpty()) {
            return Optional.empty();
        }

        final List<Double> certainPeriodList = stockPriceList.stream()
                .filter(stockPrice -> submitDate.get().minusDays(lastDays).isBefore(stockPrice.getTargetDate()))
                .filter(stockPrice -> submitDate.get().isAfter(stockPrice.getTargetDate()))
                .map(StockPriceEntity::getStockPrice)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (certainPeriodList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(certainPeriodList.stream()
                    .map(BigDecimal::valueOf)
                    // sum
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    // average
                    .divide(BigDecimal.valueOf(certainPeriodList.size()), SECOND_DECIMAL_PLACE, RoundingMode.HALF_UP));
        }
    }

    /**
     * 株価情報がデータベースに存在するか
     *
     * @param code               企業コード
     * @param targetDateAsString 対象日
     * @return boolean
     */
    private boolean isEmptyStockPrice(final String code, final String targetDateAsString) {
        final LocalDate targetDate;
        if (targetDateAsString.contains("/")) {
            targetDate = LocalDate.parse(targetDateAsString, DateTimeFormatter.ofPattern("yyyy/M/d"));
        } else {
            targetDate = LocalDate.parse(targetDateAsString);
        }
        return stockPriceDao.selectByCodeAndDate(code, targetDate).isEmpty();
    }

    /**
     * みんかぶ情報がデータベースに存在するか
     *
     * @param code               企業コード
     * @param targetDateAsString 対象日
     * @return boolean
     */
    private boolean isPresentMinkabu(final String code, final String targetDateAsString) {
        final LocalDate targetDate = MonthDay.parse(targetDateAsString, DateTimeFormatter.ofPattern("MM/dd"))
                .atYear(LocalDate.now().getYear());

        return minkabuDao.selectByCodeAndDate(code, targetDate).isPresent();
    }
}

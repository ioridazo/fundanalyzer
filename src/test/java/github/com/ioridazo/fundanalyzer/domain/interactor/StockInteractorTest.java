package github.com.ioridazo.fundanalyzer.domain.interactor;

import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.StockScraping;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.Kabuoji3ResultBean;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.MinkabuResultBean;
import github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean.NikkeiResultBean;
import github.com.ioridazo.fundanalyzer.domain.domain.specification.CompanySpecification;
import github.com.ioridazo.fundanalyzer.domain.domain.specification.DocumentSpecification;
import github.com.ioridazo.fundanalyzer.domain.domain.specification.StockSpecification;
import github.com.ioridazo.fundanalyzer.domain.value.Company;
import github.com.ioridazo.fundanalyzer.domain.value.Document;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerScrapingException;
import github.com.ioridazo.fundanalyzer.web.model.CodeInputData;
import github.com.ioridazo.fundanalyzer.web.model.DateInputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StockInteractorTest {

    private CompanySpecification companySpecification;
    private DocumentSpecification documentSpecification;
    private StockSpecification stockSpecification;
    private StockScraping stockScraping;

    private StockInteractor stockInteractor;

    @BeforeEach
    void setUp() {
        companySpecification = Mockito.mock(CompanySpecification.class);
        documentSpecification = Mockito.mock(DocumentSpecification.class);
        stockSpecification = Mockito.mock(StockSpecification.class);
        stockScraping = Mockito.mock(StockScraping.class);

        stockInteractor = Mockito.spy(new StockInteractor(
                companySpecification,
                documentSpecification,
                stockSpecification,
                stockScraping
        ));
    }

    @Nested
    class importStockPrice {

        Document document = defaultDocument();
        Company company = defaultCompany();

        @DisplayName("importStockPrice : 並列で株価を取得する")
        @Test
        void date() {
            DateInputData inputData = DateInputData.of(LocalDate.parse("2021-05-15"));

            when(documentSpecification.targetList(inputData)).thenReturn(List.of(document));
            when(companySpecification.findCompanyByEdinetCode("edinetCode")).thenReturn(Optional.of(company));
            doNothing().when(stockInteractor).importStockPrice(CodeInputData.of("code"));

            assertDoesNotThrow(() -> stockInteractor.importStockPrice(inputData));
            verify(stockInteractor, times(1)).importStockPrice((CodeInputData) any());
        }

        @DisplayName("importStockPrice : 株価を取得する")
        @Test
        void code() {
            CodeInputData inputData = CodeInputData.of("code");

            assertDoesNotThrow(() -> stockInteractor.importStockPrice(inputData));
            verify(stockSpecification, times(1)).insert(eq("code0"), (NikkeiResultBean) any());
            //noinspection unchecked
            verify(stockSpecification, times(1)).insert(eq("code0"), (List<Kabuoji3ResultBean>) any());
            verify(stockSpecification, times(1)).insert(eq("code0"), (MinkabuResultBean) any());
        }

        @DisplayName("importStockPrice : エラー発生したとき")
        @Test
        void exception() {
            CodeInputData inputData = CodeInputData.of("code");
            when(stockScraping.minkabu("code0")).thenThrow(FundanalyzerScrapingException.class);

            assertDoesNotThrow(() -> stockInteractor.importStockPrice(inputData));
            verify(stockSpecification, times(1)).insert(eq("code0"), (NikkeiResultBean) any());
            //noinspection unchecked
            verify(stockSpecification, times(1)).insert(eq("code0"), (List<Kabuoji3ResultBean>) any());
            verify(stockSpecification, times(0)).insert(eq("code0"), (MinkabuResultBean) any());
        }
    }

    @Nested
    class deleteStockPrice {

        @DisplayName("deleteStockPrice : 削除対象の株価をカウントする")
        @Test
        void count() {
            when(stockSpecification.findTargetDateToDelete()).thenReturn(List.of(
                    LocalDate.parse("2020-06-06"), LocalDate.parse("2020-06-05"), LocalDate.parse("2020-05-06")
            ));
            when(stockSpecification.delete(any())).thenReturn(1);
            assertEquals(3, stockInteractor.deleteStockPrice());
        }
    }

    private Document defaultDocument() {
        return new Document(
                null,
                null,
                null,
                "edinetCode",
                null,
                LocalDate.now(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    private Company defaultCompany() {
        return new Company(
                "code",
                null,
                null,
                "edinetCode",
                null,
                null,
                null,
                null,
                null
        );
    }
}
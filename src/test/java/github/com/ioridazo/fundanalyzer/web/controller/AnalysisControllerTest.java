package github.com.ioridazo.fundanalyzer.web.controller;

import github.com.ioridazo.fundanalyzer.domain.service.AnalysisService;
import github.com.ioridazo.fundanalyzer.domain.service.ViewService;
import github.com.ioridazo.fundanalyzer.web.model.BetweenDateInputData;
import github.com.ioridazo.fundanalyzer.web.model.CodeInputData;
import github.com.ioridazo.fundanalyzer.web.model.DateInputData;
import github.com.ioridazo.fundanalyzer.web.model.IdInputData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisControllerTest {

    private AnalysisService analysisService;
    private ViewService viewService;

    private AnalysisController controller;

    @BeforeEach
    void setUp() {
        analysisService = Mockito.mock(AnalysisService.class);
        viewService = Mockito.mock(ViewService.class);

        controller = new AnalysisController(analysisService, viewService);
    }

    @DisplayName("doMain : 指定提出日の書類をメインの一連処理をする")
    @Test
    void doMain() {
        assertEquals("redirect:/fundanalyzer/v1/index", controller.doMain("2021-05-29", "2021-05-29"));
        Mockito.verify(analysisService, Mockito.times(1))
                .doMain(BetweenDateInputData.of(LocalDate.parse("2021-05-29"), LocalDate.parse("2021-05-29")));
    }

    @DisplayName("updateView : 表示をアップデートする")
    @Test
    void updateView() {
        var actual = UriComponentsBuilder.fromUriString(controller.updateView()).build();

        assertEquals("/fundanalyzer/v1/index", actual.getPath());
        assertEquals("表示アップデート処理を要求しました。", UriUtils.decode(Objects.requireNonNull(actual.getQueryParams().getFirst("message")), "UTF-8"));
        Mockito.verify(viewService, Mockito.times(1)).updateView();
    }

    @DisplayName("scrapeByDate : 指定提出日の書類を分析する")
    @Test
    void scrapeByDate() {
        assertEquals("redirect:/fundanalyzer/v1/index", controller.scrapeByDate("2021-05-29"));
        Mockito.verify(analysisService, Mockito.times(1))
                .doByDate(DateInputData.of(LocalDate.parse("2021-05-29")));
    }

    @DisplayName("scrapeById : 指定書類IDを分析する")
    @Test
    void scrapeById() {
        assertEquals("redirect:/fundanalyzer/v1/index", controller.scrapeById("test1234,test5678"));
        Mockito.verify(analysisService, Mockito.times(1)).doById(IdInputData.of("test1234"));
        Mockito.verify(analysisService, Mockito.times(1)).doById(IdInputData.of("test5678"));
    }

    @DisplayName("importStock : 指定日に提出した企業の株価を取得する")
    @Test
    void importStock1() {
        assertEquals("redirect:/fundanalyzer/v1/index", controller.importStock("2021-05-29", "2021-05-29"));
        Mockito.verify(analysisService, Mockito.times(1))
                .importStock(BetweenDateInputData.of(LocalDate.parse("2021-05-29"), LocalDate.parse("2021-05-29")));
    }

    @DisplayName("importStock : 企業の株価を取得する")
    @Test
    void importStock2() {
        assertEquals("redirect:/fundanalyzer/v1/corporate/1234", controller.importStock("12345"));
        Mockito.verify(analysisService, Mockito.times(1)).importStock(CodeInputData.of("12345"));
    }
}
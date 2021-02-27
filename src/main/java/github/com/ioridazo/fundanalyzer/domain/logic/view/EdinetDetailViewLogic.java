package github.com.ioridazo.fundanalyzer.domain.logic.view;

import github.com.ioridazo.fundanalyzer.domain.dao.master.CompanyDao;
import github.com.ioridazo.fundanalyzer.domain.dao.transaction.DocumentDao;
import github.com.ioridazo.fundanalyzer.domain.entity.BsEnum;
import github.com.ioridazo.fundanalyzer.domain.entity.DocumentStatus;
import github.com.ioridazo.fundanalyzer.domain.entity.PlEnum;
import github.com.ioridazo.fundanalyzer.domain.entity.master.Company;
import github.com.ioridazo.fundanalyzer.domain.entity.transaction.Document;
import github.com.ioridazo.fundanalyzer.domain.logic.analysis.AnalysisLogic;
import github.com.ioridazo.fundanalyzer.domain.logic.view.bean.EdinetDetailViewBean;
import github.com.ioridazo.fundanalyzer.domain.logic.view.bean.EdinetListViewDao;
import github.com.ioridazo.fundanalyzer.domain.util.Converter;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerCalculateException;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Component
public class EdinetDetailViewLogic {

    private final AnalysisLogic analysisLogic;
    private final CompanyDao companyDao;
    private final DocumentDao documentDao;
    private final EdinetListViewDao edinetListViewDao;

    public EdinetDetailViewLogic(
            final AnalysisLogic analysisLogic,
            final CompanyDao companyDao,
            final DocumentDao documentDao,
            final EdinetListViewDao edinetListViewDao) {
        this.analysisLogic = analysisLogic;
        this.companyDao = companyDao;
        this.documentDao = documentDao;
        this.edinetListViewDao = edinetListViewDao;
    }

    /**
     * 対象提出日の未処理書類リストを取得する
     *
     * @param documentTypeCode   書類種別コード
     * @param submitDate         対象提出日
     * @param allTargetCompanies 処理対象となるすべての会社
     * @return 象提出日の未処理書類情報
     */
    @NewSpan("EdinetDetailViewLogic.edinetDetailView")
    public EdinetDetailViewBean edinetDetailView(
            final String documentTypeCode,
            final LocalDate submitDate,
            final List<Company> allTargetCompanies) {

        final var cantScrapedList = documentDao.selectByTypeAndSubmitDate(documentTypeCode, submitDate).stream()
                // filter companyCode is present
                .filter(d -> Converter.toCompanyCode(d.getEdinetCode(), allTargetCompanies).isPresent())
                // filter removed
                .filter(Document::getNotRemoved)
                .filter(d -> {
                    if (!DocumentStatus.DONE.toValue().equals(d.getScrapedBs())) {
                        // filter scrapedBs is not done
                        return true;
                    } else if (!DocumentStatus.DONE.toValue().equals(d.getScrapedPl())) {
                        // filter scrapedPl is not done
                        return true;
                        // filter scrapedNumberOfShares is not done
                    } else return !DocumentStatus.DONE.toValue().equals(d.getScrapedNumberOfShares());
                })
                .collect(Collectors.toList());

        return new EdinetDetailViewBean(
                // 対象提出日の処理状況
                edinetListViewDao.selectBySubmitDate(submitDate),
                // 提出日に関連する未処理ドキュメントのリスト
                cantScrapedList.stream()
                        .map(document -> new EdinetDetailViewBean.DocumentDetail(
                                companyDao.selectByEdinetCode(document.getEdinetCode()).orElse(Company.ofNull()),
                                document,
                                valuesForAnalysis(document)
                        )).collect(Collectors.toList())
        );
    }

    /**
     * 対象書類のスクレイピング結果を取得する
     *
     * @param document 対象書類
     * @return スクレイピング結果
     */
    private EdinetDetailViewBean.ValuesForAnalysis valuesForAnalysis(final Document document) {
        final var company = companyDao.selectByEdinetCode(document.getEdinetCode()).orElseThrow();
        final var period = document.getPeriod();

        return new EdinetDetailViewBean.ValuesForAnalysis(
                fsValue(company, BsEnum.TOTAL_CURRENT_ASSETS, period, analysisLogic::bsValues),
                fsValue(company, BsEnum.TOTAL_INVESTMENTS_AND_OTHER_ASSETS, period, analysisLogic::bsValues),
                fsValue(company, BsEnum.TOTAL_CURRENT_LIABILITIES, period, analysisLogic::bsValues),
                fsValue(company, BsEnum.TOTAL_FIXED_LIABILITIES, period, analysisLogic::bsValues),
                fsValue(company, PlEnum.OPERATING_PROFIT, period, analysisLogic::plValues),
                nsValue(company, period, analysisLogic::nsValue)
        );
    }

    private <T> Long fsValue(
            final Company company,
            final T t,
            final LocalDate period,
            final TriFunction<Company, T, LocalDate, Long> triFunction) {
        try {
            return triFunction.apply(company, t, period);
        } catch (FundanalyzerCalculateException e) {
            return null;
        }
    }

    private Long nsValue(
            final Company company,
            final LocalDate period,
            final BiFunction<Company, LocalDate, Long> biFunction) {
        try {
            return biFunction.apply(company, period);
        } catch (FundanalyzerCalculateException e) {
            return null;
        }
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }
}

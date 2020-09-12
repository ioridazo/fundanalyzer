package github.com.ioridazo.fundanalyzer.domain.scraping;

import github.com.ioridazo.fundanalyzer.domain.dao.master.BsSubjectDao;
import github.com.ioridazo.fundanalyzer.domain.dao.master.CompanyDao;
import github.com.ioridazo.fundanalyzer.domain.dao.master.ScrapingKeywordDao;
import github.com.ioridazo.fundanalyzer.domain.dao.transaction.DocumentDao;
import github.com.ioridazo.fundanalyzer.domain.dao.transaction.EdinetDocumentDao;
import github.com.ioridazo.fundanalyzer.domain.dao.transaction.FinancialStatementDao;
import github.com.ioridazo.fundanalyzer.domain.entity.BsEnum;
import github.com.ioridazo.fundanalyzer.domain.entity.DocumentStatus;
import github.com.ioridazo.fundanalyzer.domain.entity.FinancialStatementEnum;
import github.com.ioridazo.fundanalyzer.domain.entity.master.Company;
import github.com.ioridazo.fundanalyzer.domain.entity.master.Detail;
import github.com.ioridazo.fundanalyzer.domain.entity.master.ScrapingKeyword;
import github.com.ioridazo.fundanalyzer.domain.entity.transaction.Document;
import github.com.ioridazo.fundanalyzer.domain.entity.transaction.EdinetDocument;
import github.com.ioridazo.fundanalyzer.domain.entity.transaction.FinancialStatement;
import github.com.ioridazo.fundanalyzer.domain.file.FileOperator;
import github.com.ioridazo.fundanalyzer.domain.scraping.jsoup.HtmlScraping;
import github.com.ioridazo.fundanalyzer.domain.scraping.jsoup.bean.Unit;
import github.com.ioridazo.fundanalyzer.edinet.EdinetProxy;
import github.com.ioridazo.fundanalyzer.edinet.entity.request.AcquisitionRequestParameter;
import github.com.ioridazo.fundanalyzer.edinet.entity.request.AcquisitionType;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerFileException;
import github.com.ioridazo.fundanalyzer.exception.FundanalyzerRestClientException;
import lombok.extern.slf4j.Slf4j;
import org.seasar.doma.jdbc.UniqueConstraintException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedRuntimeException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class ScrapingLogic {

    private final String pathEdinet;
    private final String pathDecode;
    private final EdinetProxy proxy;
    private final FileOperator fileOperator;
    private final HtmlScraping htmlScraping;
    private final CompanyDao companyDao;
    private final DocumentDao documentDao;
    private final EdinetDocumentDao edinetDocumentDao;
    private final BsSubjectDao bsSubjectDao;
    private final FinancialStatementDao financialStatementDao;
    private final ScrapingKeywordDao scrapingKeywordDao;

    public ScrapingLogic(
            @Value("${settings.file.path.edinet}") final String pathEdinet,
            @Value("${settings.file.path.decode}") final String pathDecode,
            final EdinetProxy proxy,
            final FileOperator fileOperator,
            final HtmlScraping htmlScraping,
            final CompanyDao companyDao,
            final DocumentDao documentDao,
            final EdinetDocumentDao edinetDocumentDao,
            final BsSubjectDao bsSubjectDao,
            final FinancialStatementDao financialStatementDao,
            final ScrapingKeywordDao scrapingKeywordDao) {
        this.pathEdinet = pathEdinet;
        this.pathDecode = pathDecode;
        this.proxy = proxy;
        this.fileOperator = fileOperator;
        this.htmlScraping = htmlScraping;
        this.companyDao = companyDao;
        this.documentDao = documentDao;
        this.edinetDocumentDao = edinetDocumentDao;
        this.bsSubjectDao = bsSubjectDao;
        this.financialStatementDao = financialStatementDao;
        this.scrapingKeywordDao = scrapingKeywordDao;
    }

    public void download(final LocalDate targetDate, final String docId) {
        try {
            log.info("書類のダウンロードおよびzipファイルの解凍処理を実行します。\t書類管理番号:{}", docId);

            // ファイル取得
            proxy.acquisition(
                    makeTargetPath(pathEdinet, targetDate),
                    new AcquisitionRequestParameter(docId, AcquisitionType.DEFAULT)
            );

            documentDao.update(Document.builder()
                    .documentId(docId)
                    .downloaded(DocumentStatus.DONE.toValue())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );

            // ファイル解凍
            fileOperator.decodeZipFile(
                    makeTargetPath(pathEdinet, targetDate, docId),
                    makeTargetPath(pathDecode, targetDate, docId)
            );

            log.info("書類のダウンロードおよびzipファイルの解凍処理が正常に実行されました。");

            documentDao.update(Document.builder()
                    .documentId(docId)
                    .decoded(DocumentStatus.DONE.toValue())
                    .updatedAt(LocalDateTime.now())
                    .build());

        } catch (FundanalyzerRestClientException e) {
            log.error("書類のダウンロード処理に失敗しました。スタックトレースから原因を確認してください。" +
                            "\t処理対象日:{}\t書類管理番号:{}",
                    targetDate, docId
            );
            documentDao.update(Document.builder()
                    .documentId(docId)
                    .downloaded(DocumentStatus.ERROR.toValue())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
        } catch (IOException e) {
            log.error("zipファイルの解凍処理に失敗しました。スタックトレースから原因を確認してください。" +
                            "\t処理対象日:{}\t書類管理番号:{}",
                    targetDate, docId
            );
            documentDao.update(Document.builder()
                    .documentId(docId)
                    .decoded(DocumentStatus.ERROR.toValue())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
        }
    }

    public <T extends Detail> void scrape(
            final FinancialStatementEnum fs,
            final String documentId,
            final LocalDate date,
            final List<T> detailList) {
        final var edinetDocument = edinetDocumentDao.selectByDocId(documentId);
        final var company = companyDao.selectByEdinetCode(edinetDocument.getEdinetCode().orElse(null));
        final var targetDirectory = makeDocumentPath(pathDecode, date, documentId);

        try {
            final var targetFile = findTargetFile(targetDirectory, fs);
            if (FinancialStatementEnum.BALANCE_SHEET.equals(fs) ||
                    FinancialStatementEnum.PROFIT_AND_LESS_STATEMENT.equals(fs)) {
                // 貸借対照表、損益計算書
                insertFinancialStatement(
                        targetFile.getFirst(),
                        targetFile.getSecond(),
                        fs,
                        company,
                        detailList,
                        edinetDocument
                );

                if (FinancialStatementEnum.BALANCE_SHEET.equals(fs)) {
                    checkBs(company, edinetDocument);
                }
            } else if (FinancialStatementEnum.TOTAL_NUMBER_OF_SHARES.equals(fs)) {
                // 株式総数
                insertFinancialStatement(
                        company,
                        FinancialStatementEnum.TOTAL_NUMBER_OF_SHARES,
                        "0",
                        edinetDocument,
                        parseValue(htmlScraping.findNumberOfShares(targetFile.getFirst(), targetFile.getSecond().getKeyword())).orElse(null)
                );
            }

            log.info("次のスクレイピング情報を正常に登録しました。\n企業コード:{}\tEDINETコード:{}\t財務諸表名:{}\tファイル名:{}",
                    company.getCode().orElseThrow(),
                    company.getEdinetCode(),
                    fs.getName(),
                    targetFile.getFirst().getPath()
            );

            documentDao.update(Document.ofUpdated(
                    fs,
                    documentId,
                    DocumentStatus.DONE,
                    targetFile.getFirst().getPath(),
                    LocalDateTime.now()
            ));

        } catch (FundanalyzerFileException e) {
            documentDao.update(Document.ofUpdated(
                    fs,
                    documentId,
                    DocumentStatus.ERROR,
                    null,
                    LocalDateTime.now()
            ));
            log.error("スクレイピング処理の過程でエラー発生しました。スタックトレースを参考に原因を確認してください。" +
                            "\n企業コード:{}\tEDINETコード:{}\t財務諸表名:{}\tファイルパス:{}",
                    company.getCode().orElseThrow(),
                    company.getEdinetCode(),
                    fs.getName(),
                    targetDirectory.getPath()
            );
        }
    }

    private Pair<File, ScrapingKeyword> findTargetFile(
            final File targetFile,
            final FinancialStatementEnum financialStatement) throws FundanalyzerFileException {
        final var scrapingKeywordList = scrapingKeywordDao.selectByFinancialStatementId(
                financialStatement.toValue());

        log.info("\"{}\" のスクレイピング処理を開始します。", financialStatement.getName());

        for (ScrapingKeyword scrapingKeyword : scrapingKeywordList) {
            try {
                final var file = htmlScraping.findFile(targetFile, scrapingKeyword.getKeyword()).orElseThrow();

                log.info("対象ファイルの存在を正常に確認できました。\t財務諸表名:{}\tキーワード:{}",
                        scrapingKeyword.getRemarks(), scrapingKeyword.getKeyword()
                );

                return Pair.of(file, scrapingKeyword);

            } catch (NoSuchElementException ignored) {
                log.info("次のキーワードに合致するファイルは存在しませんでした。\t財務諸表名:{}\tキーワード:{}",
                        scrapingKeyword.getRemarks(), scrapingKeyword.getKeyword()
                );
            }
        }
        throw new FundanalyzerFileException();
    }

    private <T extends Detail> void insertFinancialStatement(
            final File targetFile,
            final ScrapingKeyword scrapingKeyword,
            final FinancialStatementEnum financialStatement,
            final Company company,
            final List<T> detailList,
            final EdinetDocument edinetDocument) throws FundanalyzerFileException {
        final var resultBeans = htmlScraping.scrapeFinancialStatement(targetFile, scrapingKeyword.getKeyword());

        resultBeans.forEach(resultBean -> detailList.stream()
                // スクレイピング結果とマスタから一致するものをフィルターにかける
                .filter(detail -> Objects.equals(resultBean.getSubject().orElse(null), detail.getName()))
                .findAny()
                // 一致するものが存在したら下記
                .ifPresent(detail -> insertFinancialStatement(
                        company,
                        financialStatement,
                        detail.getId(),
                        edinetDocument,
                        parseValue(resultBean.getCurrentValue(), resultBean.getUnit()).orElse(null)
                )));
    }

    @Transactional
    private void insertFinancialStatement(
            final Company company,
            final FinancialStatementEnum financialStatement,
            final String dId,
            final EdinetDocument edinetDocument,
            final Long value) {
        try {
            financialStatementDao.insert(new FinancialStatement(
                    null,
                    company.getCode().orElse(null),
                    company.getEdinetCode(),
                    financialStatement.toValue(),
                    dId,
                    LocalDate.parse(edinetDocument.getPeriodStart().orElseThrow()),
                    LocalDate.parse(edinetDocument.getPeriodEnd().orElseThrow()),
                    value,
                    LocalDateTime.now()
            ));
        } catch (NestedRuntimeException e) {
            if (e.contains(UniqueConstraintException.class)) {
                log.info("一意制約違反のため、データベースへの登録をスキップします。" +
                                "\t企業コード:{}\t財務諸表名:{}\t科目ID:{}\t対象年:{}",
                        company.getCode().orElse(null),
                        financialStatement.getName(),
                        dId,
                        edinetDocument.getPeriodEnd().orElseThrow().substring(0, 4)
                );
            } else {
                throw e;
            }
        }
    }

    private void checkBs(final Company company, final EdinetDocument edinetDocument) {
        final var totalCurrentLiabilities = bsSubjectDao.selectByOutlineSubjectId(
                BsEnum.TOTAL_CURRENT_LIABILITIES.getOutlineSubjectId()).stream()
                .map(bsSubject -> financialStatementDao.selectByUniqueKey(
                        edinetDocument.getEdinetCode().orElse(null),
                        FinancialStatementEnum.BALANCE_SHEET.toValue(),
                        bsSubject.getId(),
                        edinetDocument.getPeriodEnd().map(d -> d.substring(0, 4)).orElse(null)
                        ).flatMap(FinancialStatement::getValue)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();

        final var totalLiabilities = bsSubjectDao.selectByOutlineSubjectId(
                BsEnum.TOTAL_LIABILITIES.getOutlineSubjectId()).stream()
                .map(bsSubject -> financialStatementDao.selectByUniqueKey(
                        edinetDocument.getEdinetCode().orElse(null),
                        FinancialStatementEnum.BALANCE_SHEET.toValue(),
                        bsSubject.getId(),
                        edinetDocument.getPeriodEnd().map(d -> d.substring(0, 4)).orElse(null)
                        ).flatMap(FinancialStatement::getValue)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();

        if (totalCurrentLiabilities.isPresent() && totalLiabilities.isPresent()) {
            if (totalCurrentLiabilities.get().equals(totalLiabilities.get())) {
                insertFinancialStatement(
                        company,
                        FinancialStatementEnum.BALANCE_SHEET,
                        bsSubjectDao.selectByUniqueKey(
                                BsEnum.TOTAL_FIXED_LIABILITIES.getOutlineSubjectId(),
                                BsEnum.TOTAL_FIXED_LIABILITIES.getDetailSubjectId()
                        ).getId(),
                        edinetDocument,
                        0L
                );

                log.info("\"貸借対照表\" の \"固定負債合計\" が存在しなかったため、次の通りとして\"0\" にてデータベースに登録しました。" +
                                "\t企業コード:{}\t書類ID:{}\t流動負債合計:{}\t負債合計:{}",
                        company.getCode().orElseThrow(),
                        edinetDocument.getDocId(),
                        totalCurrentLiabilities.get(),
                        totalLiabilities.get()
                );
            }
        }
    }

    private Optional<Long> parseValue(final String value) {
        try {
            return Optional.of(value)
                    .filter(v -> !v.isBlank())
                    .filter(v -> !" ".equals(v))
                    .map(s -> Long.parseLong(s
                            .replace("※ ", "")
                            .replace("※1", "").replace("※１", "")
                            .replace("※2", "").replace("※２", "")
                            .replace("※3", "").replace("※３", "")
                            .replace("※4", "").replace("※４", "")
                            .replace("※5", "").replace("※５", "")
                            .replace("※6", "").replace("※６", "")
                            .replace("※7", "").replace("※７", "")
                            .replace("※8", "").replace("※８", "")
                            .replace("※9", "").replace("※９", "")
                            .replace("※10", "").replace("※11", "")
                            .replace("※12", "").replace("※13", "")
                            .replace("※14", "").replace("※15", "")
                            .replace("※16", "").replace("※17", "")
                            .replace("*1", "").replace("*2", "")
                            .replace("株", "")
                            .replace("－", "0").replace("―", "0")
                            .replace(" ", "").replace(" ", "")
                            .replace(",", "")
                            .replace("△", "-")
                    ));
        } catch (NumberFormatException e) {
            log.error("数値を正常に認識できなかったため、NULLで登録します。\tvalue:{}", value);
            return Optional.empty();
        }
    }

    private Optional<Long> parseValue(final String value, final Unit unit) {
        return parseValue(value).map(l -> l * unit.getValue());
    }

    private File makeTargetPath(final String prePath, final LocalDate targetDate) {
        return new File(prePath + "/" + targetDate.getYear() + "/" + targetDate.getMonth() + "/" + targetDate);
    }

    private File makeTargetPath(final String prePath, final LocalDate targetDate, final String docId) {
        return new File(prePath + "/" + targetDate.getYear() + "/" + targetDate.getMonth() + "/" + targetDate
                + "/" + docId);
    }

    private File makeDocumentPath(final String prePath, final LocalDate targetDate, final String docId) {
        return new File(prePath + "/" + targetDate.getYear() + "/" + targetDate.getMonth() + "/" + targetDate
                + "/" + docId + "/XBRL/PublicDoc");
    }
}
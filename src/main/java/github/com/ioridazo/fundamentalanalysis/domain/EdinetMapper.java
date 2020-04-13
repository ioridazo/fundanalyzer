package github.com.ioridazo.fundamentalanalysis.domain;

import github.com.ioridazo.fundamentalanalysis.domain.entity.EdinetDocument;
import github.com.ioridazo.fundamentalanalysis.edinet.entity.response.Results;

public class EdinetMapper {

    public static EdinetDocument map(Results results) {
        return new EdinetDocument(
                results.getDocID(),
                results.getEdinetCode(),
                results.getSecCode(),
                results.getJcn(),
                results.getFilerName(),
                results.getFundCode(),
                results.getOrdinanceCode(),
                results.getFormCode(),
                results.getDocTypeCode(),
                results.getPeriodStart(),
                results.getPeriodEnd(),
                results.getSubmitDateTime(),
                results.getDocDescription(),
                results.getIssuerEdinetCode(),
                results.getSubjectEdinetCode(),
                results.getSubsidiaryEdinetCode(),
                results.getCurrentReportReason(),
                results.getParentDocID(),
                results.getOpeDateTime(),
                results.getWithdrawalStatus(),
                results.getDocInfoEditStatus(),
                results.getDisclosureStatus(),
                results.getXbrlFlag(),
                results.getPdfFlag(),
                results.getAttachDocFlag(),
                results.getEnglishDocFlag(),
                null
        );
    }
}

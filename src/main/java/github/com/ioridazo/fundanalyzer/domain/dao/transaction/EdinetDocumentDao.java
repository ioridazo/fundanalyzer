package github.com.ioridazo.fundanalyzer.domain.dao.transaction;

import github.com.ioridazo.fundanalyzer.domain.entity.transaction.EdinetDocument;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Select;
import org.seasar.doma.boot.ConfigAutowireable;
import org.seasar.doma.jdbc.Result;

import java.util.List;

@ConfigAutowireable
@Dao
public interface EdinetDocumentDao {

    @Select
    List<EdinetDocument> selectByDocTypeCode(final String docTypeCode);

    @Select
    String countByDocId(final String docId);

    @Insert
    Result<EdinetDocument> insert(final EdinetDocument edinetDocument);
}
select * from document
where
  document_type_code = /* documentTypeCode */'120'
and
  period like /* @prefix(yearOfPeriod) */'2020%'

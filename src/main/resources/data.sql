--industry
--insert into industry (name) values
--  ('sample')
--  ;

--company
--insert into company (code, company_name, industry_id, edinet_code, insert_date, update_date) values
--  ('00000', 'sample', '1', 'E00000', sysdate, sysdate)
--  ;

--scraping_keyword
insert into scraping_keyword (financial_statement_id, keyword) values
  ('1', 'jpcrp_cor:BalanceSheetTextBlock'),
  ('2', 'StatementOfIncomeTextBlock'),('2', 'jpcrp_cor:ConsolidatedStatementOfIncomeTextBlock'),
  ('4', '株式総数')
  ;

--balance_sheet_subject
insert into balance_sheet_subject (outline_subject_id, detail_subject_id, name) values
  ('1', null, '流動資産'),
--  (null, '1', '現金及び預金'), (null, '1', '信託現金及び信託預金'), (null, '1', '営業未収入金'), (null, '1', 'リース投資資産'), (null, '1', '前払費用'),
--  (null, '1', 'その他'), (null, '1', '貸倒引当金'), (null, '1', '流動資産合計'),
  ('2', null, '有形固定資産'),
--  (null, '2', '建物'), (null, '2', '減価償却累計額'), (null, '2', '建物（純額）'), (null, '2', '建物附属設備'), (null, '2', '減価償却累計額'),
--  (null, '2', '建物附属設備（純額）'), (null, '2', '構築物'), (null, '2', '減価償却累計額'), (null, '2', '構築物（純額）'), (null, '2', '機械及び装置'),
--  (null, '2', '減価償却累計額'), (null, '2', '機械及び装置（純額）'), (null, '2', '工具、器具及び備品'), (null, '2', '減価償却累計額'), (null, '2', '工具、器具及び備品（純額）'),
--  (null, '2', '土地'), (null, '2', '建設仮勘定'), (null, '2', '信託建物'), (null, '2', '減価償却累計額'), (null, '2', '信託建物（純額）'),
--  (null, '2', '信託建物附属設備'), (null, '2', '減価償却累計額'), (null, '2', '信託建物附属設備（純額）'), (null, '2', '信託構築物'), (null, '2', '減価償却累計額'),
--  (null, '2', '信託構築物（純額）'), (null, '2', '信託機械及び装置'), (null, '2', '減価償却累計額'), (null, '2', '信託機械及び装置（純額）'), (null, '2', '信託工具、器具及び備品'),
--  (null, '2', '減価償却累計額'), (null, '2', '信託工具、器具及び備品（純額）'), (null, '2', '信託土地'), (null, '2', '信託建設仮勘定'), (null, '2', '有形固定資産合計'),
  ('3', null, '無形固定資産'),
--  (null, '3', '借地権'), (null, '3', '信託借地権'), (null, '3', 'その他'), (null, '3', '無形固定資産合計'),
  ('4', null, '投資その他の資産'),
--  (null, '4', '修繕積立金'), (null, '4', '敷金及び保証金'), (null, '4', '信託差入敷金及び保証金'), (null, '4', '長期前払費用'), (null, '4', '投資その他の資産合計'),
  ('5', '5', '固定資産合計'),
  ('6', null, '繰延資産'),
--  (null, '6', '投資法人債発行費'), (null, '6', '繰延資産合計'),
  ('7', '7', '資産合計'),
  ('8', null, '流動負債'),
--  (null, '8', '営業未払金'), (null, '8', '短期借入金'), (null, '8', '8年内返済予定の長期借入金'), (null, '8', '8年内償還予定の投資法人債'), (null, '8', '未払金'),
--  (null, '8', '未払費用'), (null, '8', '未払法人税等'), (null, '8', '未払消費税等'), (null, '8', '前受金'), (null, '8', 'その他'),
--  (null, '8', '流動負債合計'),
  ('9', null, '固定負債'),
--  (null, '9', '投資法人債'), (null, '9', '長期借入金'), (null, '9', '預り敷金及び保証金'), (null, '9', '資産除去債務'), (null, '9', '固定負債合計'),
  ('10', '10', '負債合計'),
  ('11', null, '投資主資本'),
--  (null, '11', '出資総額'),
  ('12', null, '剰余金'),
--  (null, '12', '圧縮積立金'), (null, '12', '任意積立金合計'), (null, '12', '当期未処分利益又は当期未処理損失（△）'), (null, '12', '剰余金合計'),
  ('13', null, '投資主合計'),
--  (null, '13', '投資主資本合計'),
  ('14', '14', '純資産合計'),
  ('15', '15', '負債純資産合計')
  -- TODO 項目追加
 ;

--profit_and_less_statement_subject
insert into profit_and_less_statement_subject (outline_subject_id, detail_subject_id, name) values
  ('1', null, '営業収益'),
--  (null, '1', '賃貸事業収入'), (null, '1', 'その他賃貸事業収入'), (null, '1', '営業収益合計'),
  ('2', null, '営業費用'),
--  (null, '2', '賃貸事業費用'), (null, '2', '不動産等売却損'), (null, '2', '資産運用報酬'), (null, '2', '資産保管及び一般事務委託手数料'), (null, '2', '役員報酬'),
--  (null, '2', '会計監査人報酬'), (null, '2', 'その他営業費用'), (null, '2', '営業費用合計'),
  ('3', '3', '営業利益'),
  ('4', null, '営業外収益'),
--  (null, '4', '受取利息'), (null, '4', '未払分配金戻入'), (null, '4', '還付加算金'), (null, '4', 'その他'), (null, '4', '営業外収益合計'),
  ('5', null, '営業外費用'),
--  (null, '5', '支払利息'), (null, '5', '投資法人債利息'), (null, '5', '投資法人債発行費償却'), (null, '5', '融資手数料'), (null, '5', 'その他'),
--  (null, '5', '営業外費用合計'),
  ('6', '6', '経常利益'),
  ('7', null, '特別損失'),
--  (null, '7', '災害による損失'), (null, '7', '特別損失合計'),
  ('8', '8', '税引前当期純利益'),
  ('9', '9', '法人税、住民税及び事業税'),
  ('10', '10', '法人税等合計'),
  ('11', '11', '当期純利益'),
  ('12', '12', '前期繰越利益'),
  ('13', '13', '当期未処分利益又は当期未処理損失（△）')
  ;

--cash_flow_statement
--insert into cash_flow_statement_subject (id, subject) values
--  ('1', '営業活動によるキャッシュ・フロー'), ('2', '投資活動によるキャッシュ・フロー'), ('3', '財務活動によるキャッシュ・フロー'),
--  ('4', '現金及び現金同等物の増減額（△は減少）'), ('5', '現金及び現金同等物の期首残高'), ('6', '現金及び現金同等物の期末残高')
--  ;
--cash_flow_statement_subject
--insert into cash_flow_statement_detail (subject_id, name) values
-- ('1', '税引前当期純利益'), ('1', '減価償却費'), ('1', '長期前払費用償却額'), ('1', '投資法人債発行費償却'), ('1', '受取利息'),
-- ('1', '支払利息'), ('1', '固定資産除却損'), ('1', '営業未収入金の増減額（△は増加）'), ('1', '未収消費税等の増減額（△は増加）'), ('1', '未払消費税等の増減額（△は減少）'),
-- ('1', 'リース投資資産の増減額（△は増加）'), ('1', '前払費用の増減額（△は増加）'), ('1', '長期前払費用の支払額'), ('1', '信託有形固定資産の売却による減少額'), ('1', '修繕積立金の取崩額'),
-- ('1', '営業未払金の増減額（△は減少）'), ('1', '未払金の増減額（△は減少）'), ('1', '前受金の増減額（△は減少）'), ('1', 'その他'), ('1', '小計'),
-- ('1', '利息の受取額'), ('1', '利息の支払額'), ('1', '法人税等の支払額'), ('1', '営業活動によるキャッシュ・フロー'),
-- ('2', '定期預金の預入による支出'), ('2', '定期預金の払戻による収入'), ('2', '有形固定資産の取得による支出'), ('2', '信託有形固定資産の取得による支出'), ('2', '預り敷金及び保証金の受入による収入'),
-- ('2', '預り敷金及び保証金の返還による支出'), ('2', '敷金及び保証金の差入による支出'), ('2', '信託差入敷金及び保証金の差入による支出'), ('2', '信託差入敷金及び保証金の回収による収入'), ('2', '使途制限付信託預金の預入による支出'),
-- ('2', '使途制限付信託預金の払戻による収入'), ('2', '修繕積立金の支出'), ('2', '投資活動によるキャッシュ・フロー'),
-- ('3', '短期借入れによる収入'), ('3', '短期借入金の返済による支出'), ('3', '長期借入れによる収入'), ('3', '長期借入金の返済による支出'), ('3', '投資法人債の発行による収入'),
-- ('3', '投資法人債の償還による支出'), ('3', '投資法人債発行費の支出'), ('3', '分配金の支払額'), ('3', '財務活動によるキャッシュ・フロー'),
-- ('4', '現金及び現金同等物の増減額（△は減少）'),
-- ('5', '現金及び現金同等物の期首残高'),
-- ('6', '現金及び現金同等物の期末残高')
-- ;

--financial_statement
--insert into financial_statement (company_code, edinet_code, financial_statement_id, subject_id, period_start, period_end, value) values
--  ('00000', 'E00000', '1', '1', sysdate, sysdate, 1)
--  ;
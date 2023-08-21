package ac.iie.riskpredict;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.QueryHint;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional()
public interface SupplyRiskDao extends JpaRepository<SupplierRisk, String> {

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk set contract_cycle=abs(real_delivery_date - mission_accept_date) where supply_id = " +
            ":supplyId", nativeQuery = true)
    void contract_cycle(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1 SET t1.avg_history_contract_cycle = (SELECT AVG(t2.CONTRACT_CYCLE) AS avg FROM " +
            "t_risk t2 WHERE t2.supply_code = t1.supply_code GROUP BY t2.supply_code) where supply_id = :supplyId",
            nativeQuery = true)
    void avg_history_contract_cycle(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk set plan_cycle=abs(contract_date - delivery_date) where supply_id = :supplyId",
            nativeQuery = true)
    void planCycle(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.avg_yearly_plan_cycle = (SELECT AVG(plan_cycle)\n" + "             " +
            "                FROM t_risk t2\n" + "                             WHERE EXTRACT(YEAR FROM contract_date)" +
            "=:year and supply_code=:supplyCode\n" + "                             group by SUPPLY_CODE)\n" + "where " +
            "supply_id = :supplyId", nativeQuery = true)
    void avg_yearly_plan_cycle(@Param("supplyId") String supplyId, @Param("year") String year,
                               @Param("supplyCode") String supplyCode);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk sr\n" + "SET sr.history_plan_cycle = (\n" + "    SELECT AVG(plan_cycle) \n" + "    " +
            "FROM t_risk sr2 \n" + "    WHERE sr2.supply_code = sr.supply_code\n" + "    GROUP BY sr2.supply_code\n" + ") where supply_id = :supplyId", nativeQuery = true)
    void history_plan_cycle(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.avg_pack_unit_price = (SELECT AVG(t2.unit_price)\n" + "            " +
            "                   FROM t_risk t2\n" + "                               WHERE t2.tender_id = t1.tender_id" +
            " group by t2.TENDER_ID) where supply_id = :supplyId", nativeQuery = true)
    void avg_pack_unit_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk\n" + "set vari_pack_unit_price = (\n" + "  select variance(unit_price)\n" + "  from" +
            " t_risk s\n" + "  where s.tender_id = t_risk.tender_id\n" + "  group by tender_id\n" + ") where " +
            "supply_id = :supplyId", nativeQuery = true)
    void vari_pack_unit_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk\n" + "SET rank_pack_unit_price = (\n" + "  SELECT COUNT(DISTINCT t2.unit_price) + " +
            "1\n" + "  FROM t_risk t2\n" + "  WHERE t2.tender_id = t_risk.tender_id AND t2.unit_price > t_risk" +
            ".unit_price group by t2.TENDER_ID)\n" + "where supply_id = :supplyId", nativeQuery = true)
    void rank_pack_unit_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.max_pack_unit_price = (SELECT MAX(t2.unit_price)\n" + "            " +
            "                  FROM t_risk t2\n" + "                              WHERE t2.tender_id = t1.tender_id " +
            "group by t2.TENDER_ID) where supply_id = :supplyId", nativeQuery = true)
    void max_pack_unit_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.vari_yearly_unit_price = (\n" + "  SELECT VARIANCE(t2.unit_price)" +
            "\n" + "  FROM t_risk t2\n" + "  WHERE t2.supply_code = t1.supply_code\n" + "  AND EXTRACT(YEAR FROM t2" +
            ".CONTRACT_DATE) = :year group by t2.SUPPLY_CODE\n" + ") where supply_id = :supplyId", nativeQuery = true)
    void vari_yearly_unit_price(@Param("supplyId") String supplyId, @Param("year") String year);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.avg_history_total_price = (\n" + "  SELECT AVG(total_price)\n" + " " +
            " FROM t_risk t2\n" + "  WHERE t2.supply_code = t1.supply_code group by t2.SUPPLY_CODE\n" + ") where " +
            "supply_id = :supplyId", nativeQuery = true)
    void avg_history_total_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk t1\n" + "set t1.vari_history_total_price=" + " (select variance(total_price) var " +
            "from t_risk t2 where t1.supply_code = t2.supply_code group by t2.supply_code) where supply_id = " +
            ":supplyId", nativeQuery = true)
    void vari_history_total_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk t1 set t1.rank_history_total_price= (select max(unit_price) max from t_risk t2 " +
            "where t1.supply_code = t2.supply_code group by t2.supply_code) where supply_id = :supplyId",
            nativeQuery = true)
    void rank_history_total_price(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE t_risk t1\n" + "SET t1.rank_yearly_total_price = (SELECT RANK() OVER (partition by " +
            "SUPPLY_CODE ORDER BY total_price DESC)\n" + "                                  FROM t_risk t2\n" + "    " +
            "                              WHERE EXTRACT(YEAR FROM t2.CONTRACT_DATE) = :year\n" + "                  " +
            "                  AND t1.supply_code = t2.supply_code\n" + "                                  and t1" +
            ".TOTAL_PRICE=t2.TOTAL_PRICE\n" + "                                  group by t2.SUPPLY_CODE,total_price)" +
            "\n" + "where supply_id = :supplyId", nativeQuery = true)
    void rank_yearly_total_price(@Param("supplyId") String supplyId, @Param("year") String year);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk\n" + "set supply_num=(total_price / unit_price)\n" + "where supply_id = :supplyId",
            nativeQuery = true)
    void supply_num(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "update t_risk t1\n" + "set t1.vari_history_num=(select variance(supply_num) var from t_risk t2 " +
                    "where t1.supply_code = t2.supply_code group by supply_code) where supply_id = :supplyId",
            nativeQuery = true)
    void vari_history_num(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk t1\n" + "set t1.max_history_num=(select variance(supply_num) var from t_risk t2 " +
            "where t1.supply_code = t2.supply_code group by supply_code) where supply_id = :supplyId", nativeQuery =
            true)
    void max_history_num(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk t1\n" + "set t1.rank_yearly_num=(select rank() over (partition by supply_code order" +
            " by supply_num desc) as rank\n" + "                        from t_risk t2\n" + "                        " +
            "WHERE EXTRACT(YEAR FROM t2.CONTRACT_DATE) = :year\n" + "                          and t1.supply_code = " +
            "t2.supply_code\n" + "                        and t1.SUPPLY_NUM=t2.SUPPLY_NUM\n" + "                     " +
            "   group by supply_code,SUPPLY_NUM)\n" + "where supply_id = :supplyId\n", nativeQuery = true)
    void rank_yearly_num(@Param("supplyId") String supplyId, @Param("year") String year);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk t1\n" + "SET t1.history_fulfill_rate = (\n" + "    SELECT COUNT(*) / (SELECT COUNT" +
            "(*) FROM  t_risk WHERE supplier_id = t1.supplier_id group by SUPPLIER_ID)\n" + "    FROM  t_risk\n" + " " +
            "   WHERE supplier_id = t1.supplier_id AND sample_class = '1' group by t1.SUPPLIER_ID\n" + ") where " +
            "supply_id = :supplyId", nativeQuery = true)
    void history_fulfill_rate(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk\n" + "SET fulfill_times = (\n" + "  SELECT COUNT(*) \n" + "  FROM  t_risk t \n" +
            "  WHERE t.supplier_id =  t_risk.supplier_id \n" + "  AND t.sample_class = '1' group by SUPPLIER_ID\n" +
            ") where supply_id = :supplyId", nativeQuery = true)
    void fulfill_times(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk t1\n" + "SET t1.history_breach_rate = (\n" + "    SELECT COUNT(*) / (SELECT COUNT" +
            "(*) FROM  t_risk WHERE supplier_id = t1.supplier_id group by SUPPLIER_ID)\n" + "    FROM  t_risk\n" + " " +
            "   WHERE supplier_id = t1.supplier_id AND sample_class = '0' group by t1.SUPPLIER_ID\n" + ") where " +
            "supply_id = :supplyId", nativeQuery = true)
    void history_breach_rate(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk\n" + "SET history_breach_times = (\n" + "  SELECT COUNT(*) \n" + "  FROM  t_risk t" +
            " \n" + "  WHERE t.supplier_id =  t_risk.supplier_id \n" + "  AND t.sample_class = '0' group by " +
            "SUPPLIER_ID\n" + ") where supply_id = :supplyId", nativeQuery = true)
    void history_breach_times(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk t1 \n" + "SET t1.max_history_breach_amount = (\n" + "    SELECT MAX(total_price) " +
            "\n" + "    FROM  t_risk t2 \n" + "    WHERE t2.sample_class = '0' \n" + "    AND t1.supplier_id = t2" +
            ".supplier_id\n" + ") where supply_id = :supplyId", nativeQuery = true)
    void max_history_breach_amount(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE  t_risk t1 \n" + "SET t1.sum_history_breach_amount = (\n" + "    SELECT sum(total_price) " +
            "\n" + "    FROM  t_risk t2 \n" + "    WHERE t2.sample_class = '0' \n" + "    AND t1.supplier_id = t2" +
            ".supplier_id\n" + ") where supply_id = :supplyId", nativeQuery = true)
    void sum_history_breach_amount(@Param("supplyId") String supplyId);

    @Modifying(clearAutomatically = true)
    @Query(value = "update t_risk t1\n" + "set t1.avg_history_breach_amount=(select avg(total_price) avg from t_risk " +
            "t2 where t2.sample_class = '0' and t1.supplier_id = t2.supplier_id group by supplier_id) where supply_id" +
            " = :supplyId", nativeQuery = true)
    void avg_history_breach_amount(@Param("supplyId") String supplyId);


    @Override
    @Transactional
    @Query(value = "select count(*) from t_risk",nativeQuery = true)
    long count();

}

package ac.iie.riskpredict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 * @author a3
 */
@Service
public class RiskServiceImpl {
    @Resource
    private SupplyRiskDao dao;


    public void contract_cycle(String supplyId) {
        dao.contract_cycle(supplyId);
    }


    public void avg_history_contract_cycle(String supplyId) {
        dao.avg_history_contract_cycle(supplyId);
    }


    public void plan_cycle(String supplyId) {
        dao.planCycle(supplyId);
    }


    public void avg_yearly_plan_cycle(String supplyId, String year, String supplyCode) {
        dao.avg_yearly_plan_cycle(supplyId, year, supplyCode);
    }


    public void history_plan_cycle(String supplyId) {
        dao.history_plan_cycle(supplyId);

    }


    public void avg_pack_unit_price(String supplyId) {
        dao.avg_pack_unit_price(supplyId);

    }


    public void vari_pack_unit_price(String supplyId) {
        dao.vari_pack_unit_price(supplyId);
    }


    public void rank_pack_unit_price(String supplyId) {
        dao.rank_pack_unit_price(supplyId);
    }


    public void max_pack_unit_price(String supplyId) {
        dao.max_pack_unit_price(supplyId);

    }


    public void vari_yearly_unit_price(String supplyId, String year) {
        dao.vari_yearly_unit_price(supplyId, year);
    }

    public void avg_history_total_price(String supplyId) {
        dao.avg_history_total_price(supplyId);
    }

    public void vari_history_total_price(String supplyId) {
        dao.vari_history_total_price(supplyId);
    }


    public void rank_history_total_price(String supplyId) {
        dao.rank_history_total_price(supplyId);
    }


    public void rank_yearly_total_price(String supplyId, String year) {
        dao.rank_yearly_total_price(supplyId, year);
    }


    public void supply_num(String supplyId) {
        dao.supply_num(supplyId);
    }

    public void vari_history_num(String supplyId) {
        dao.vari_history_num(supplyId);
    }

    public void max_history_num(String supplyId) {
        dao.max_history_num(supplyId);
    }

    public void rank_yearly_num(String supplyId, String year) {
        dao.rank_yearly_num(supplyId, year);
    }

    public void history_fulfill_rate(String supplyId) {
        dao.history_fulfill_rate(supplyId);
    }

    public void fulfill_times(String supplyId) {
        dao.fulfill_times(supplyId);
    }

    public void history_breach_rate(String supplyId) {
        dao.history_breach_rate(supplyId);
    }

    public void history_breach_times(String supplyId) {
        dao.history_breach_times(supplyId);
    }

    public void max_history_breach_amount(String supplyId) {
        dao.max_history_breach_amount(supplyId);
    }

    public void sum_history_breach_amount(String supplyId) {
        dao.sum_history_breach_amount(supplyId);
    }

    public void avg_history_breach_amount(String supplyId) {
        dao.avg_history_breach_amount(supplyId);
    }

    public long count() {
        return dao.count();
    }

    @Transactional
    public Page<SupplierRisk> findAll(Pageable pageable) {
        return dao.findAll(pageable);
    }

    @Transactional
    public SupplierRisk save(SupplierRisk risk) {
        return dao.save(risk);
    }

}
